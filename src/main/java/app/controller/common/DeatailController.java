package app.controller.common;

import app.data.redis.HashData;
import app.data.redis.RedisData;
import app.data.zk.ZkData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: pickjob@126.com
 * @time: 2020-06-22
 **/
public class DeatailController extends BaseController {
    private static final Logger logger = LogManager.getLogger(DeatailController.class);
    @FXML private VBox container;

    @Override
    public void init() {
        if (env == null) {
            return;
        }
        if (env instanceof RedisData) {
            RedisData redisData = (RedisData) env;
            Label keyLabel = new Label("KEY:");
            TextField keyTextField = new TextField(redisData.getCanonicalName());
            Label valueLabel = new Label("VALUE:");
            switch (redisData.getType()) {
                case STRING:
                    String v = (String) redisData.getValue();
                    if (v.startsWith("{")) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
                            TextArea valueTextField = new TextArea(writer.writeValueAsString(objectMapper.readValue(v, Map.class)));
                            container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
                        } catch (JsonProcessingException e) {
                            logger.error(e.getMessage(), e);
                        }
                        ;
                    } else if (v.startsWith("[")) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
                            TextArea valueTextField = new TextArea(writer.writeValueAsString(objectMapper.readValue(v, List.class)));
                            container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
                        } catch (JsonProcessingException e) {
                            logger.error(e.getMessage(), e);
                        }
                        ;
                    } else {
                        TextArea valueTextField = new TextArea(v);
                        container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
                    }
                    break;
                case LIST:
                    List<String> listItems = (List<String>) redisData.getValue();
                    ListView<String> valueListView = new ListView<>(FXCollections.observableList(listItems));
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueListView);
                    break;
                case SET:
                    Set<String> setItems = (Set<String>) redisData.getValue();
                    ListView<String> setListView = new ListView<>(FXCollections.observableList(new ArrayList<>(setItems)));
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, setListView);
                    break;
                case HASH:
                    Map<String, String> map = (Map<String, String>) redisData.getValue();
                    List<HashData> list = new ArrayList<>();
                    for (String key : map.keySet()) {
                        list.add(new HashData(key, map.get(key)));
                    }
                    ObservableList<HashData> items = FXCollections.observableList(list);
                    TableView<HashData> tableView = new TableView<>();
                    TableColumn<HashData, String> keyCol = new TableColumn("Key");
                    keyCol.setCellValueFactory(new PropertyValueFactory<>("Key"));
                    keyCol.setCellFactory((TableColumn<HashData, String> column) -> {
                        return new TableCell<HashData, String>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(item);
                                setTooltip(new Tooltip(item));
                            }
                        };
                    });
                    TableColumn<HashData, String> valueCol = new TableColumn("Value");
                    valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
                    valueCol.setCellFactory((TableColumn<HashData, String> column) -> {
                        return new TableCell<HashData, String>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(item);
                                setTooltip(new Tooltip(item));
                            }
                        };
                    });
                    keyCol.prefWidthProperty().bind(tableView.widthProperty().divide(2).subtract(5));
                    valueCol.prefWidthProperty().bind(tableView.widthProperty().divide(2).subtract(5));
                    tableView.getColumns().addAll(keyCol, valueCol);
                    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                    tableView.setItems(items);
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, tableView);
                    break;
                default:
                    break;
            }
        } else if (env instanceof ZkData) {
            ZkData zkData = (ZkData) env;
            Label keyLabel = new Label("KEY:");
            TextField keyTextField = new TextField(zkData.getCanonicalName());
            Label valueLabel = new Label("VALUE:");
            String v = zkData.getValue();
            if (v.startsWith("{")) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
                    TextArea valueTextField = new TextArea(writer.writeValueAsString(objectMapper.readValue(v, Map.class)));
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
                } catch (JsonProcessingException e) {
                    logger.error(e.getMessage(), e);
                }
                ;
            } else if (v.startsWith("[")) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
                    TextArea valueTextField = new TextArea(writer.writeValueAsString(objectMapper.readValue(v, List.class)));
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
                } catch (JsonProcessingException e) {
                    logger.error(e.getMessage(), e);
                }
                ;
            } else {
                TextArea valueTextField = new TextArea(v);
                container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
            }
        }
    }
}
