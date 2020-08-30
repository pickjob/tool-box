package app.controller;

import app.enums.LoginKeyEnum;
import app.controller.common.BaseController;
import app.data.redis.RedisData;
import app.data.redis.RedisDataType;
import app.data.TreeNode;
import app.scheduler.JavaFxScheduler;
import app.util.Constants;
import app.util.TreeNodeUtils;
import app.util.StageUtils;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URLEncoder;
import java.util.EnumMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-08
 **/
public class RedisController extends BaseController {
    private static final Logger logger = LogManager.getLogger(RedisController.class);
    private EnumMap<LoginKeyEnum, String> loginEnumMap;
    private volatile boolean isLoad = false;
    private RedisCommands<String, String> redisCommands;
    @FXML private TextField keyFilterTextField;
    @FXML private TreeTableView<RedisData> keyValueTreeTableView;

    @Override
    public void buildUIComponents() {
        TreeTableColumn<RedisData, String> keyColumn = new TreeTableColumn<>("KEY");
        keyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            StringProperty result = null;
            if (redisData.getType() == RedisDataType.NONE) {
                result = new ReadOnlyStringWrapper(redisData.getKey());
            } else {
                result = new ReadOnlyStringWrapper(redisData.getKey() + "[" + RedisDataType.toString(redisData.getType()) + "]");
            }
            return result;
        });
        TreeTableColumn<RedisData, String> valueColumn = new TreeTableColumn<>("VALUE");
        valueColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            StringProperty result = null;
            if (redisData.getType() == RedisDataType.NONE) {
                result = new ReadOnlyStringWrapper("");
            } else {
                result = new ReadOnlyStringWrapper(redisData.getValue() == null ? "" : redisData.getValue() + "");
            }
            return result;
        });
        TreeTableColumn<RedisData, String> ttlColumn = new TreeTableColumn<>("TTL");
        ttlColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            StringProperty result = null;
            if (redisData.getTtl() != null) {
                result = new ReadOnlyStringWrapper(redisData.getTtl() + "");
            } else {
                result = new ReadOnlyStringWrapper("");
            }
            return result;
        });
        TreeTableColumn<RedisData, RedisData> operatorColumn = new TreeTableColumn<>("operator");
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
                private Button refreshBtn = new Button();

                @Override
                protected void updateItem(RedisData item, boolean empty) {
                    if (item == getItem()) {
                        return;
                    }
                    super.updateItem(item, empty);
                    if (item == null) {
                        super.setText(null);
                        super.setGraphic(null);
                    } else {
                        refreshBtn.setGraphic(new FontIcon(FontAwesome.REFRESH));
                        refreshBtn.setOnMouseClicked(event -> {
                            // TODO: Refresh Redis Key & Value
                            logger.info("TODO");
                        });
                        setGraphic(refreshBtn);
                    }
                }
            };
        });
        keyColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        valueColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(5).subtract(1));
        ttlColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).subtract(1));
        operatorColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        keyValueTreeTableView.getColumns().addAll(keyColumn, valueColumn, ttlColumn, operatorColumn);
        keyValueTreeTableView.setRowFactory(treeTableView -> {
            TreeTableRow<RedisData> row = new TreeTableRow();
            row.setOnMouseClicked(event -> {
                RedisData data = row.getItem();
                if (data != null && data.getType() != RedisDataType.NONE) {
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/fxml/common/detail.fxml"));
                        Parent content = loader.load();
                        final BaseController controller = loader.getController();
                        controller.setEnv(row.getTreeItem().getValue());
                        controller.init();
                        Scene detailScene = new Scene(content);
                        detailScene.getStylesheets().addAll(Constants.loadStyleSheets());
                        Stage detailStage = new Stage();
                        detailStage.setScene(detailScene);
                        detailStage.setTitle("Redis Detail");
                        StageUtils.quarterScreeStage(detailStage);
                        detailStage.initModality(Modality.WINDOW_MODAL);
                        detailStage.initOwner(keyValueTreeTableView.getScene().getWindow());
                        detailStage.showAndWait();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
            return row;
        });
    }

    @Override
    public void init() {
        if (!isLoad) {
            if (env != null && env instanceof EnumMap) {
                loginEnumMap = (EnumMap<LoginKeyEnum, String>)env;
                loadRedisTreeView(env);
            }
        }
    }

    @Override
    public boolean isNeedLogin() {
        return true;
    }

    private void loadRedisTreeView(Object env) {
        try {
            TreeNode<RedisData> rootRedisTreeNode = new TreeNode<>("Root", null);
            Observable.<String>create(emitter -> {
                buildRedisCommand();
                ScanArgs scanArgs = ScanArgs.Builder.limit(10);
                for (KeyScanCursor<String> keyScanCursor = redisCommands.scan(scanArgs); true; ) {
                    for (String key : keyScanCursor.getKeys()) {
                        emitter.onNext(key);
                    }
                    if (keyScanCursor.isFinished()) {
                        emitter.onComplete();
                        break;
                    } else {
                        keyScanCursor = redisCommands.scan(keyScanCursor);
                    }
                }
            }).subscribeOn(Schedulers.newThread())
                    .buffer(5, TimeUnit.SECONDS)
                    .map(list -> {
                        for (String key : list) {
                            TreeNodeUtils.appendKeyToTreeNode(rootRedisTreeNode, key, ":");
                        }
                        return rootRedisTreeNode;
                    })
                    .map(rootTreeNode -> {
                        TreeNodeUtils.iteratorTreeNode(rootRedisTreeNode, redisTreeNode -> {
                            if (redisTreeNode.getTreeItem() == null) {
                                if (redisTreeNode.getCanonicalName() == null) {
                                    // 当前节点为Root节点
                                    RedisData redisData = new RedisData(redisTreeNode.getName(), redisTreeNode.getCanonicalName());
                                    redisData.setType(RedisDataType.NONE);
                                    TreeItem<RedisData> treeItem = new TreeItem<>(redisData);
                                    redisTreeNode.setValue(redisData);
                                    redisTreeNode.setTreeItem(treeItem);
                                } else {
                                    RedisData redisData = new RedisData(redisTreeNode.getName(), redisTreeNode.getCanonicalName());
                                    redisData.setType(RedisDataType.value(redisCommands.type(redisTreeNode.getCanonicalName())));
                                    switch (redisData.getType()) {
                                        case STRING:
                                            redisData.setValue(redisCommands.get(redisData.getCanonicalKey()));
                                            break;
                                        case LIST:
                                            redisData.setValue(redisCommands.lrange(redisData.getCanonicalKey(), 0, -1));
                                            break;
                                        case SET:
                                            redisData.setValue(redisCommands.smembers(redisData.getCanonicalKey()));
                                            break;
                                        case HASH:
                                            redisData.setValue(redisCommands.hgetall(redisData.getCanonicalKey()));
                                            break;
                                        default:
                                            break;
                                    }
                                    if (redisData.getType() != RedisDataType.NONE) {
                                        redisData.setTtl(redisCommands.ttl(redisTreeNode.getCanonicalName()));
                                    }
                                    TreeItem<RedisData> treeItem = new TreeItem<>(redisData);
                                    redisTreeNode.setValue(redisData);
                                    redisTreeNode.setTreeItem(treeItem);
                                }
                            }
                        });
                        TreeNodeUtils.buildTreeItemRelation(rootTreeNode);
                        return rootRedisTreeNode.getTreeItem();
                    })
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(redisTreeItem -> {
                        keyValueTreeTableView.setRoot(redisTreeItem);
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void buildRedisCommand() throws Exception {
        if (redisCommands == null || !redisCommands.isOpen()) {
            String host = loginEnumMap.get(LoginKeyEnum.HOST);
            String port = loginEnumMap.get(LoginKeyEnum.PORT);
            String index = loginEnumMap.get(LoginKeyEnum.INDEX);
            String password = loginEnumMap.get(LoginKeyEnum.PASSWORD);
            StringBuilder redisUrlBuilder = new StringBuilder("redis://");
            if (StringUtils.isNotBlank(password)) {
                redisUrlBuilder.append(URLEncoder.encode(password, "UTF-8"));
            }
            if (StringUtils.isNoneBlank(host)) {
                redisUrlBuilder.append("@").append(host);
            }
            if (StringUtils.isNotBlank(port)) {
                redisUrlBuilder.append(":").append(port);
            }
            if (StringUtils.isNotBlank(index)) {
                redisUrlBuilder.append("/").append(index);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("redisUrl: {}", redisUrlBuilder);
            }
            RedisClient redisClient = RedisClient.create(redisUrlBuilder.toString());
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            redisCommands = connection.sync();
        }
    }
}
