package app.controller;

import app.common.Context;
import app.controller.common.BaseController;
import app.controller.common.DeatailController;
import app.data.redis.RedisData;
import app.data.redis.RedisDataType;
import app.util.splitor.TreeNode;
import app.util.splitor.TreeNodeUtils;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-08
 **/
public class RedisController extends BaseController {
    private static final Logger logger = LogManager.getLogger(RedisController.class);
    @FXML TextField keyFilterTextField;
    @FXML TreeTableView<RedisData<?>> keyValueTreeTableView;

    @Override
    public void run() {
        try {
            RedisCommands<String, String> redisCommands = buildRedisCommand();
            new Thread(() -> {
                TreeNode<RedisData<?>> rootRedisData = buildRedisTreeNode(redisCommands);
                TreeItem<RedisData<?>> root = new TreeItem<>();
                buildTreeItem(root, rootRedisData);
                Platform.runLater(()-> {
                    keyValueTreeTableView.setRoot(root);
                });
            }).start();
            TreeTableColumn<RedisData<?>, String> key = new TreeTableColumn<>("KEY");
            key.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData<?>, String> p) -> {
                RedisData<?> redisData = p.getValue().getValue();
                return new ReadOnlyStringWrapper(redisData.getKey() + "[" + RedisDataType.toString(redisData.getType()) + "]");
            });
            TreeTableColumn<RedisData<?>, String> type = new TreeTableColumn<>("value");
            type.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData<?>, String> p) -> {
                RedisData<?> redisData = p.getValue().getValue();
                return new ReadOnlyStringWrapper(RedisDataType.toString(redisData.getType()));
            });
            TreeTableColumn<RedisData<?>, String> ttl = new TreeTableColumn<>("ttl");
            ttl.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData<?>, String> p) -> {
                RedisData<?> redisData = p.getValue().getValue();
                if (redisData.getTtl() != null) {
                    return new ReadOnlyStringWrapper(redisData.getTtl() + "");
                } else {
                    return new ReadOnlyStringWrapper("");
                }
            });
            TreeTableColumn<RedisData<?>, RedisData<?>> operator = new TreeTableColumn<>("operator");
            operator.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData<?>, RedisData<?>> p) -> {
                RedisData<?> redisData = p.getValue().getValue();
                return new ObjectBinding<RedisData<?>>() {
                    @Override
                    protected RedisData<?> computeValue() {
                        return redisData;
                    }
                };
            });
            operator.setCellFactory((TreeTableColumn<RedisData<?>, RedisData<?>> column) -> {
                return new TreeTableCell<RedisData<?>, RedisData<?>>() {
                    private Button btn = new Button("Refresh");

                    @Override
                    protected void updateItem(RedisData<?> item, boolean empty) {
                        btn.setGraphic(new FontIcon(FontAwesome.REFRESH));
                        btn.setOnMouseClicked(event -> {
                            // TODO: Refresh Redis Key & Value
                            logger.info("TODO");
                        });
                        setGraphic(btn);
                    }
                };
            });

            key.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(4).subtract(1));
            type.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(4).subtract(1));
            ttl.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(4).subtract(1));
            operator.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(4).subtract(1));

            keyValueTreeTableView.getColumns().addAll(key, type, ttl, operator);
            keyValueTreeTableView.setRowFactory(treeTableView -> {
                TreeTableRow<RedisData<?>> row = new TreeTableRow();
                row.setOnMouseClicked(event -> {
                    RedisData<?> data = row.getItem();
                    if (data != null && data.getType() != RedisDataType.NONE) {
                        Stage stage = DeatailController.createDetailStage(data, redisCommands);
                        stage.show();
                    }
                });
                return row;
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isNeedLogin() {
        return true;
    }

    private void buildTreeItem(TreeItem<RedisData<?>> rootTreeItem, TreeNode<RedisData<?>> redisDataTreeNode) {
        rootTreeItem.setValue(redisDataTreeNode.getData());
        rootTreeItem.setExpanded(true);
        for (TreeNode<RedisData<?>> node : redisDataTreeNode.getChildren()) {
            RedisData<?> redisData = node.getData();
            if (redisData != null) {
                TreeItem<RedisData<?>> tmpTreeItem = new TreeItem<>(redisData);
                rootTreeItem.getChildren().add(tmpTreeItem);
                buildTreeItem(tmpTreeItem, node);
            }
        }
    }

    private TreeNode<RedisData<?>> buildRedisTreeNode(RedisCommands<String, String> redisCommands) {
        List<String> keys = new ArrayList<>();
        ScanArgs scanArgs = ScanArgs.Builder.limit(10);
        for (KeyScanCursor<String> keyScanCursor = redisCommands.scan(scanArgs); true;) {
            for (String key : keyScanCursor.getKeys()) {
                if (logger.isDebugEnabled()) logger.debug("redis key: {}", key);
                keys.add(key);
            }
            if (keyScanCursor.isFinished()) {
                break;
            } else {
                keyScanCursor = redisCommands.scan(keyScanCursor);
            }
        }
        TreeNode<RedisData<?>> treeNode = TreeNodeUtils.buildTree(keys, ":", key -> {
            if (key == null) return new RedisData<>("ROOT");
            RedisData<?> redisData = new RedisData<>(key);
            redisData.setType(RedisDataType.value(redisCommands.type(key)));
            if (redisData.getType() != RedisDataType.NONE) {
                redisData.setTtl(redisCommands.ttl(key));
            }
            return redisData;
        });
        return treeNode;
    }

    private RedisCommands<String, String> buildRedisCommand() throws Exception {
        String host = context.getLoginEnum(Context.LoginKeyEnum.HOST);
        String port = context.getLoginEnum(Context.LoginKeyEnum.PORT);
        String index = context.getLoginEnum(Context.LoginKeyEnum.INDEX);
        String password = context.getLoginEnum(Context.LoginKeyEnum.PASSWORD);
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
        if (logger.isDebugEnabled()) logger.debug("redisUrl: {}", redisUrlBuilder.toString());
        RedisClient redisClient = RedisClient.create(redisUrlBuilder.toString());
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        return connection.sync();
    }
}
