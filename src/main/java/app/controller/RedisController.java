package app.controller;

import app.components.DetailDialog;
import app.config.Config;
import app.config.RedisConfig;
import app.controller.common.TreeBaseController;
import app.data.TreeNode;
import app.data.redis.RedisData;
import app.data.redis.RedisDataType;
import app.scheduler.JavaFxScheduler;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: pickjob@126.com
 * @date: 2020-04-08
 **/
public class RedisController extends TreeBaseController<RedisData> implements Initializable {
    private static final Logger logger = LogManager.getLogger(RedisController.class);
    private static final String SPLITTER = ":";
    private RedisConfig defaultConfig = new RedisConfig();
    private RedisCommands<String, String> redisCommands;
    @FXML private FontIcon searchBtn;
    @FXML private TextField searchText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeTableColumn<RedisData, String> keyColumn = new TreeTableColumn<>("KEY");
        keyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            if (StringUtils.isBlank(redisData.getCanonicalName())) {
                return new ReadOnlyStringWrapper(redisData.getName());
            } else {
                return new ReadOnlyStringWrapper(redisData.getCanonicalName());
            }
        });
        keyColumn.setCellFactory((TreeTableColumn<RedisData, String> column) -> {
            return new TreeTableCell<RedisData, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (StringUtils.isNotBlank(item)) {
                        super.setText(item);
                        setTooltip(new Tooltip(item));
                    } else {
                        super.setText(null);
                        super.setGraphic(null);
                    }
                }
            };
        });
        TreeTableColumn<RedisData, String> summary = new TreeTableColumn<>("SUMMARY");
        summary.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            String result = null;
            if (redisData.getValue() != null) {
                result = redisData.getType() + "[";
                if ((redisData.getValue() + "").length() > 20) {
                    result += (redisData.getValue() + "").substring(0, 20) + "...]";
                } else {
                    result += (redisData.getValue() + "") + "]";
                }
            }
            return new ReadOnlyStringWrapper(result);
        });
        summary.setCellFactory((TreeTableColumn<RedisData, String> column) -> {
            return new TreeTableCell<RedisData, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (StringUtils.isNotBlank(item)) {
                        super.setText(item);
                        setTooltip(new Tooltip(item));
                    } else {
                        super.setText(null);
                        super.setGraphic(null);
                    }
                }
            };
        });
        TreeTableColumn<RedisData, String> ttlColumn = new TreeTableColumn<>("TTL");
        ttlColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getTtl() == null ? null : redisData.getTtl() + "");
        });
        TreeTableColumn<RedisData, RedisData> operatorColumn = new TreeTableColumn<>("OPERATOR");
        operatorColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, RedisData> p) -> {
            RedisData redisData = p.getValue().getValue();
            return new ObjectBinding<RedisData>() {
                @Override
                protected RedisData computeValue() {
                    return redisData;
                }
            };
        });
        operatorColumn.setCellFactory((TreeTableColumn<RedisData, RedisData> column) -> {
            return new TreeTableCell<RedisData, RedisData>() {
                HBox hBox = new HBox();
                FontIcon refreshIcon = new FontIcon(FontAwesome.REFRESH);
                FontIcon trashIcon = new FontIcon(FontAwesome.TRASH);
                FontIcon moreIcon = new FontIcon(FontAwesome.COMMENT);

                {
                    hBox.setAlignment(Pos.CENTER);
                    hBox.getChildren().addAll(refreshIcon, trashIcon, moreIcon);
                }

                @Override
                protected void updateItem(RedisData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getMocked()) {
                        refreshIcon.setOnMouseClicked(event -> {
                            loadTreeView(item.getCanonicalName());
                        });
                        trashIcon.setOnMouseClicked(event -> {
                            deleteKey(item.getCanonicalName());
                            loadTreeView(null);
                        });
                        moreIcon.setOnMouseClicked(event -> {
                            try {
                                DetailDialog detailDialog = new DetailDialog(item);
                                detailDialog.showAndWait();
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        });
                        setGraphic(hBox);
                    } else {
                        super.setText(null);
                        super.setGraphic(null);
                    }
                }
            };
        });
        keyColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        summary.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(5).subtract(1));
        ttlColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).subtract(1));
        operatorColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        keyValueTreeTableView.getColumns().addAll(keyColumn, summary, ttlColumn, operatorColumn);
        if (rootTreeNode == null) {
            rootTreeNode = buildTreeNode("ROOT", true);
            rootTreeNode.getValue().setCanonicalName(null);
            rootTreeNode.setTreeItem(new TreeItem<>(rootTreeNode.getValue()));
        }
        keyValueTreeTableView.setRoot(rootTreeNode.getTreeItem());
        searchBtn.setOnMouseClicked(event -> {
            String search = searchText.getText();
            if (StringUtils.isNoneBlank(search)) {
                Set<TreeNode<RedisData>> showSet = new HashSet<>();
                Set<TreeNode<RedisData>> hideSet = new HashSet<>();
                filter(rootTreeNode, name -> name.contains(search), showSet, hideSet);
            } else {
                buildTreeItem(rootTreeNode, null);
            }
        });
    }

    @Override
    public Config loadDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public void loadTreeView(String reloadKey) {
        logger.info("reloadKey: {}", reloadKey);
        Observable.fromSupplier(() -> {
            if (redisCommands != null && redisCommands.getStatefulConnection().isOpen()) {
                return redisCommands;
            }
            RedisConfig conf = defaultConfig;
            StringBuilder redisUrlBuilder = new StringBuilder("redis://");
            if (StringUtils.isNotBlank(conf.getPassword())) {
                redisUrlBuilder.append(URLEncoder.encode(conf.getPassword(), "UTF-8"));
            }
            if (StringUtils.isNoneBlank(conf.getHost())) {
                redisUrlBuilder.append("@").append(conf.getHost());
            }
            if (StringUtils.isNotBlank(conf.getPort())) {
                redisUrlBuilder.append(":").append(conf.getPort());
            }
            if (StringUtils.isNotBlank(conf.getIndex())) {
                redisUrlBuilder.append("/").append(conf.getIndex());
            }
            RedisClient redisClient = RedisClient.create(redisUrlBuilder.toString());
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            redisCommands = connection.sync();
            keyValueTreeTableView.getScene()
                    .getWindow()
                    .setOnCloseRequest(windowEvent -> {
                        logger.info("closing ...");
                        if (redisCommands != null && redisCommands.getStatefulConnection().isOpen()) {
                            try {
                                redisCommands.getStatefulConnection().close();
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    });
            return redisCommands;
        })
                .subscribeOn(Schedulers.single())
                .flatMap(redisCommands -> {
                    return Observable.<TreeNode<RedisData>>create(emitter -> {
                        if (StringUtils.isBlank(reloadKey)) {
                            ScanArgs scanArgs = ScanArgs.Builder.limit(10);
                            for (KeyScanCursor<String> keyScanCursor = redisCommands.scan(scanArgs); true; ) {
                                for (String key : keyScanCursor.getKeys()) {
                                    emitter.onNext(buildTreeNode(key, false));
                                }
                                if (keyScanCursor.isFinished()) {
                                    break;
                                } else {
                                    keyScanCursor = redisCommands.scan(keyScanCursor);
                                }
                            }
                        } else {
                            emitter.onNext(buildTreeNode(reloadKey, false));
                        }
                        emitter.onComplete();
                    });
                })
                .buffer(3, TimeUnit.SECONDS)
                .map(list -> {
                    TreeNode<RedisData> root = rootTreeNode;
                    if (StringUtils.isBlank(reloadKey)) {
                        root.getChildren().clear();
                    }
                    for (TreeNode<RedisData> treeNode : list) {
                        appendOrReplaceTreeNode(root, treeNode, SPLITTER);
                    }
                    return root;
                })
                .observeOn(JavaFxScheduler.platform())
                .subscribe(root -> {
                    if (StringUtils.isBlank(reloadKey)) {
                        root.getTreeItem().getChildren().clear();
                    }
                    buildTreeItem(root, reloadKey);
                    selectedOneBackTracing(root, reloadKey);
                    keyValueTreeTableView.refresh();
                });
        ;
    }

    @Override
    public void deleteKey(String key) {
        redisCommands.del(key);
    }

    @Override
    public TreeNode<RedisData> buildTreeNode(String canonicalName, Boolean mocked) {
        TreeNode<RedisData> treeNode = new TreeNode<>();
        RedisData redisData = new RedisData();
        List<String> pieces = treeKeys(canonicalName, SPLITTER);
        redisData.setName(pieces.get(pieces.size() - 1));
        redisData.setCanonicalName(canonicalName);
        if (mocked) {
            redisData.setMocked(true);
            treeNode.setValue(redisData);
            return treeNode;
        }
        redisData.setMocked(false);
        redisData.setType(RedisDataType.value(redisCommands.type(canonicalName)));
        redisData.setTtl(redisCommands.ttl(canonicalName));
        switch (redisData.getType()) {
            case STRING:
                redisData.setValue(redisCommands.get(canonicalName));
                break;
            case LIST:
                redisData.setValue(redisCommands.lrange(canonicalName, 0, -1));
                break;
            case SET:
                redisData.setValue(redisCommands.smembers(canonicalName));
                break;
            case HASH:
                redisData.setValue(redisCommands.hgetall(canonicalName));
                break;
            default:
                logger.info("unknown type: {}", redisCommands.type(canonicalName));
                break;
        }
        treeNode.setValue(redisData);
        return treeNode;
    }
}
