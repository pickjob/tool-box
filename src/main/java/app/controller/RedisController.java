package app.controller;

import app.common.Context;
import app.controller.common.BaseController;
import app.data.redis.RedisData;
import app.data.redis.RedisDataType;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-08
 **/
public class RedisController extends BaseController {
    private static final Logger logger = LogManager.getLogger(RedisController.class);
    @FXML TextField keyFilterTextField;
    @FXML TreeTableView keyValueTreeTableView;

    @Override
    public void run() {
        try {
            RedisCommands<String, String> redisCommands = buildRedisCommand();
            new Thread(() -> {
                RedisData<?> rootRedisData = buildRedisData(redisCommands);
                TreeItem<RedisData<?>> root = new TreeItem<>(rootRedisData);
                buildTreeItem(root, rootRedisData);
                Platform.runLater(()-> {
                    keyValueTreeTableView.setRoot(root);
                });
            }).start();
            keyValueTreeTableView.getColumns().addAll(buildTreeTableColumn(redisCommands));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isNeedLogin() {
        return true;
    }

    private Object[] buildTreeTableColumn(RedisCommands<String, String> redisCommands) {
        TreeTableColumn<RedisData<?>, String> key = new TreeTableColumn<>("KEY");
        key.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData<?>, String> p) -> {
            RedisData<?> redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getKey());
        });
        TreeTableColumn<RedisData<?>, String> type = new TreeTableColumn<>("TYPE");
        type.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData<?>, String> p) -> {
            RedisData<?> redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(RedisDataType.toString(redisData.getType()));
        });
        TreeTableColumn<RedisData<?>, RedisData<?>> value = new TreeTableColumn<>("value");
        value.setCellFactory( (TreeTableColumn<RedisData<?>, RedisData<?>> column) -> {
            return new TreeTableCell<RedisData<?>, RedisData<?>>() {

                @Override
                protected void updateItem(RedisData<?> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        switch (item.getType()) {
                            case STRING:
                                setText(item.getValue() + "");
                                break;
                            case NONE:
                                setText("");
                                break;
                            case UNKNOW:
                                break;
                        }
                    }
                }
            };
        } );
        value.setCellValueFactory((TreeTableColumn.CellDataFeatures<RedisData<?>, RedisData<?>> p) -> {
            RedisData<?> redisData = p.getValue().getValue();
            return new ObjectBinding<RedisData<?>>() {
                @Override
                protected RedisData<?> computeValue() {
                    return redisData;
                }
            };
        });
        key.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(3));
        value.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(3));
        type.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(3));
        Object[] array = new TreeTableColumn[3];
        array[0] = key;
        array[1] = type;
        array[2] = value;
        return array;
    }

    private void buildTreeItem(TreeItem<RedisData<?>> rootTreeItem, RedisData<?> rootRedisData) {
        rootTreeItem.setExpanded(true);
        for (String k : rootRedisData.getChildren().keySet()) {
            RedisData<?> redisData = rootRedisData.getChildren().get(k);
            if (redisData != null) {
                TreeItem<RedisData<?>> tmpTreeItem = new TreeItem<>(redisData);
                rootTreeItem.getChildren().add(tmpTreeItem);
                buildTreeItem(tmpTreeItem, redisData);
            }
        }
    }

    private RedisData<?> buildRedisData(RedisCommands<String, String> redisCommands) {
        RedisData<?> rootRedisData = new RedisData<>("Root");
        ScanArgs scanArgs = ScanArgs.Builder
                .limit(10);
        KeyScanCursor<String> keyScanCursor = redisCommands.scan(scanArgs);
        while (!keyScanCursor.isFinished()) {
            for (String key : keyScanCursor.getKeys()) {
                Map<String, RedisData<?>> m = rootRedisData.getChildren();
                StringBuilder sb = new StringBuilder();
                String[] keyPieces = key.split(":");
                for (int i = 0; i < keyPieces.length; i++) {
                    if (i > 0) {
                        sb.append(keyPieces[i -1]).append(":");
                    }
                    String keyPiece = keyPieces[i];
                    if (!m.containsKey(keyPiece)) {
                        RedisData<?> data = new RedisData<>(sb.toString() + keyPiece);
                        data.setType(RedisDataType.value(redisCommands.type(sb.toString() + keyPiece)));
                        switch (data.getType()) {
                            case STRING:
                                ((RedisData<String>)data).setValue(redisCommands.get(data.getKey()));
                                break;
                            default:
                                break;
                        }
                        m.put(keyPiece, data);
                    }
                    m = m.get(keyPiece).getChildren();
                }
            }
            keyScanCursor = redisCommands.scan(keyScanCursor, scanArgs);
        }
        return rootRedisData;
    }

    private RedisCommands<String, String> buildRedisCommand() throws Exception {
        String host = context.getLoginEnumMap().get(Context.LoginKeyEnum.HOST);
        String port = context.getLoginEnumMap().get(Context.LoginKeyEnum.PORT);
        String index = context.getLoginEnumMap().get(Context.LoginKeyEnum.INDEX);
        String password = context.getLoginEnumMap().get(Context.LoginKeyEnum.PASSWORD);
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
        RedisClient redisClient = RedisClient.create(redisUrlBuilder.toString());
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        return connection.sync();
    }
}
