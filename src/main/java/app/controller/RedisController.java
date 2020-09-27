package app.controller;

import app.controller.common.BaseController;
import app.data.TreeNode;
import app.data.redis.RedisData;
import app.data.redis.RedisDataType;
import app.enums.LoginKeyEnum;
import app.scheduler.JavaFxScheduler;
import app.util.Constants;
import app.util.StageUtils;
import app.util.TreeNodeUtils;
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
    private static final String SPLITTER = ":";
    private EnumMap<LoginKeyEnum, String> loginEnumMap;
    private volatile boolean isLoad = false;
    private RedisCommands<String, String> redisCommands;
    private TreeNode<RedisData> rootRedisTreeNode;
    @FXML private TextField keyFilterTextField;
    @FXML private TreeTableView<RedisData> keyValueTreeTableView;

    @Override
    public void buildUIComponents() {
        TreeTableColumn<RedisData, String> keyColumn = new TreeTableColumn<>("KEY");
        keyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getCanonicalName());
        });
        TreeTableColumn<RedisData, String> valueColumn = new TreeTableColumn<>("VALUE");
        valueColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getValue() == null ? null : redisData.getValue() + "");
        });
        TreeTableColumn<RedisData, String> ttlColumn = new TreeTableColumn<>("TTL");
        ttlColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData, String> p) -> {
            RedisData redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getTtl() == null ? null : redisData.getTtl() + "");
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
                    super.updateItem(item, empty);
                    if (item == null) {
                        super.setText(null);
                        super.setGraphic(null);
                    } else if (item.getValue() != null) {
                        refreshBtn.setGraphic(new FontIcon(FontAwesome.REFRESH));
                        refreshBtn.setOnMouseClicked(event -> {
                            loadTreeView(item.getCanonicalName());
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
                RedisData data = row.getTreeItem().getValue();
                if (data != null && data.getValue() != null) {
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/fxml/common/detail.fxml"));
                        Parent content = loader.load();
                        final BaseController controller = loader.getController();
                        controller.setEnv(data);
                        controller.init();
                        Scene detailScene = new Scene(content);
                        detailScene.getStylesheets().addAll(Constants.loadStyleSheets());
                        Stage detailStage = new Stage();
                        detailStage.setScene(detailScene);
                        detailStage.setTitle("Redis Detail");
                        StageUtils.quarterScreeStage(detailStage, false);
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
        keyValueTreeTableView.setPlaceholder(new Label("Ohh, it's empty."));
    }

    @Override
    public void init() {
        if (!isLoad) {
            if (rootRedisTreeNode == null) {
                rootRedisTreeNode = new TreeNode<>("Root", null);
                TreeItem<RedisData> rootTreeItem = new TreeItem<>(new RedisData());
                rootRedisTreeNode.setTreeItem(rootTreeItem);
                keyValueTreeTableView.setRoot(rootRedisTreeNode.getTreeItem());
            }
            if (env != null && env instanceof EnumMap) {
                loginEnumMap = (EnumMap<LoginKeyEnum, String>) env;
                loadTreeView(null);
            }
        }
    }

    @Override
    public boolean isNeedLogin() {
        return true;
    }

    private void loadTreeView(String reloadKey) {
        try {
            Observable.<RedisCommands>fromSupplier(() -> {
                if (redisCommands != null) {
                    return redisCommands;
                }
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
                return redisCommands;
            })
                    .subscribeOn(Schedulers.single())
                    .<TreeNode<RedisData>>flatMap(zooKeeper -> {
                        return Observable.<TreeNode<RedisData>>create(emitter -> {
                            if (StringUtils.isBlank(reloadKey)) {
                                ScanArgs scanArgs = ScanArgs.Builder.limit(10);
                                for (KeyScanCursor<String> keyScanCursor = redisCommands.scan(scanArgs); true; ) {
                                    for (String key : keyScanCursor.getKeys()) {
                                        emitter.onNext(TreeNodeUtils.buildTreeNode(key, buildData(key), SPLITTER));
                                    }
                                    if (keyScanCursor.isFinished()) {
                                        break;
                                    } else {
                                        keyScanCursor = redisCommands.scan(keyScanCursor);
                                    }
                                }
                            } else {
                                emitter.onNext(TreeNodeUtils.buildTreeNode(reloadKey, buildData(reloadKey), SPLITTER));
                            }
                            emitter.onComplete();
                        });
                    })
                    .buffer(3, TimeUnit.SECONDS)
                    .<TreeNode<RedisData>>map(list -> {
                        for (TreeNode<RedisData> treeNode: list) {
                            TreeNodeUtils.appendOrReplaceTreeNode(rootRedisTreeNode, treeNode, SPLITTER, RedisData::new);
                        }
                        return rootRedisTreeNode;
                    })
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe(rootTreeNode -> {
                        TreeNodeUtils.buildTreeItem(rootTreeNode);
                        isLoad = true;
                    });
            ;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private RedisData buildData(String key) {
        RedisData redisData = new RedisData();
        redisData.setType(RedisDataType.value(redisCommands.type(key)));
        redisData.setTtl(redisCommands.ttl(key));
        switch (redisData.getType()) {
            case STRING:
                redisData.setValue(redisCommands.get(key));
                break;
            case LIST:
                redisData.setValue(redisCommands.lrange(key, 0, -1));
                break;
            case SET:
                redisData.setValue(redisCommands.smembers(key));
                break;
            case HASH:
                redisData.setValue(redisCommands.hgetall(key));
                break;
            default:
                break;
        }
        return redisData;
    }
}
