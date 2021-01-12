package app.components;

import app.controller.common.BaseController;
import app.data.redis.HashData;
import app.data.redis.RedisData;
import app.data.zk.ZkData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.*;

/**
 * @author: pickjob@126.com
 * @date: 2020-06-22
 **/
public class DetailController extends BaseController implements Initializable {
    private static final Logger logger = LogManager.getLogger(DetailController.class);
    @FXML private VBox container;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public boolean isNeedLogin() {
        return false;
    }

    @Override
    public void loadWithData(Object data) {
        if (data == null) {
            return;
        }
        if (data instanceof RedisData) {
            RedisData redisData = (RedisData) data;
            Label keyLabel = new Label("KEY:");
            TextField keyTextField = new TextField(redisData.getCanonicalName());
            Label valueLabel = new Label("VALUE:");
            container.getChildren().addAll(keyLabel, keyTextField, valueLabel);
            switch (redisData.getType()) {
                case STRING:
                    String v = (String) redisData.getValue();
                    TextArea valueTextField = null;
                    if (v.startsWith("{")) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
                            valueTextField = new TextArea(writer.writeValueAsString(objectMapper.readValue(v, Map.class)));
                            container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
                            valueTextField.prefHeightProperty().bind(container.heightProperty());
                        } catch (JsonProcessingException e) {
                            logger.error(e.getMessage(), e);
                        }
                        ;
                    } else if (v.startsWith("[")) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
                            valueTextField = new TextArea(writer.writeValueAsString(objectMapper.readValue(v, List.class)));
                            container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueTextField);
                            valueTextField.prefHeightProperty().bind(container.heightProperty());
                        } catch (JsonProcessingException e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        valueTextField = new TextArea(v);
                    }
                    container.getChildren().add(valueTextField);
                    valueTextField.prefHeightProperty().bind(container.heightProperty());
                    break;
                case LIST:
                    List<String> listItems = (List<String>) redisData.getValue();
                    ListView<String> valueListView = new ListView<>(FXCollections.observableList(listItems));
                    valueListView.setEditable(true);
                    valueListView.setCellFactory(TextFieldListCell.forListView());
                    container.getChildren().add(valueListView);
                    valueListView.prefHeightProperty().bind(container.heightProperty());
                    break;
                case SET:
                    Set<String> setItems = (Set<String>) redisData.getValue();
                    ListView<String> setListView = new ListView<>(FXCollections.observableList(new ArrayList<>(setItems)));
                    setListView.setEditable(true);
                    setListView.setCellFactory(TextFieldListCell.forListView());
                    container.getChildren().add(setListView);
                    setListView.prefHeightProperty().bind(container.heightProperty());
                    break;
                case HASH:
                    Map<String, String> map = (Map<String, String>) redisData.getValue();
                    List<HashData> list = new ArrayList<>();
                    for (String key : map.keySet()) {
                        list.add(new HashData(key, map.get(key)));
                    }
                    ObservableList<HashData> items = FXCollections.observableList(list);
                    TableView<HashData> tableView = new TableView<>();
                    TableColumn<HashData, String> keyCol = new TableColumn<>("Key");
                    keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
                    keyCol.setCellFactory(TextFieldTableCell.<HashData>forTableColumn());
                    TableColumn<HashData, String> valueCol = new TableColumn("Value");
                    valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
                    valueCol.setCellFactory(TextFieldTableCell.<HashData>forTableColumn());
                    keyCol.prefWidthProperty().bind(tableView.widthProperty().divide(2).subtract(5));
                    valueCol.prefWidthProperty().bind(tableView.widthProperty().divide(2).subtract(5));
                    tableView.getColumns().addAll(keyCol, valueCol);
                    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                    tableView.setItems(items);
                    tableView.setEditable(true);
                    container.getChildren().add(tableView);
                    tableView.prefHeightProperty().bind(container.heightProperty());
                    break;
                default:
                    break;
            }
        } else if (data instanceof ZkData) {
            ZkData zkData = (ZkData) data;
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
