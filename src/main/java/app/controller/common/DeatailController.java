package app.controller.common;

import app.common.Context;
import app.data.redis.HashData;
import app.data.redis.RedisData;
import app.util.stage.StageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.lettuce.core.api.sync.RedisCommands;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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
    private Object data;
    @FXML private VBox container;

    @Override
    public void init(Context context) {
        super.init(context);
        if (data instanceof RedisData) {
            RedisData<?> redisData = (RedisData<?>) data;
            Label keyLabel = new Label("KEY:");
            TextField keyTextField = new TextField(((RedisData<String>)redisData).getKey());
            Label valueLabel = new Label("VALUE:");
            switch (redisData.getType()) {
                case STRING:
                    String v = ((RedisData<String>) redisData).getValue();
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
                    List<String> listItems = ((RedisData<List<String>>) redisData).getValue();
                    ListView<String> valueListView = new ListView<>(FXCollections.observableList(listItems));
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, valueListView);
                    break;
                case SET:
                    Set<String> setItems = ((RedisData<Set<String>>) redisData).getValue();
                    ListView<String> setListView = new ListView<>(FXCollections.observableList(new ArrayList<>(setItems)));
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, setListView);
                    break;
                case HASH:
                    Map<String, String> map = ((RedisData<Map<String, String>>) redisData).getValue();
                    List<HashData> list = new ArrayList<>();
                    for (String key : map.keySet()) {
                        list.add(new HashData(key, map.get(key)));
                    }
                    ObservableList<HashData> items = FXCollections.observableList(list);
                    TableView<HashData> tableView = new TableView<>();
                    TableColumn<HashData, String> keyCol = new TableColumn("Key");
                    keyCol.setCellValueFactory(new PropertyValueFactory<>("Key"));
                    TableColumn<HashData, String> valueCol = new TableColumn("Value");
                    valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
                    keyCol.prefWidthProperty().bind(tableView.widthProperty().divide(2).subtract(1));
                    valueCol.prefWidthProperty().bind(tableView.widthProperty().divide(2).subtract(1));
                    tableView.getColumns().addAll(keyCol, valueCol);
                    tableView.setItems(items);
                    container.getChildren().addAll(keyLabel, keyTextField, valueLabel, tableView);
                    break;
                case NONE:
                case UNKNOW:
                    break;
            }
        }
    }

    public static Stage createDetailStage(RedisData<?> redisData, RedisCommands<String, String> redisCommands) {
        Stage detailStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(DeatailController.class.getResource("/fxml/common/detail.fxml"));
            Parent content = loader.load();
            DeatailController controller = loader.getController();
            if (redisData.getValue() == null) {
                logger.info("loading data: {}", redisData.getKey());
                switch (redisData.getType()) {
                    case STRING:
                        ((RedisData<String>) redisData).setValue(redisCommands.get(redisData.getKey()));
                        break;
                    case LIST:
                        List<String> list = redisCommands.lrange(redisData.getKey(), 0, -1);
                        ((RedisData<List<String>>) redisData).setValue(list);
                        break;
                    case SET:
                        Set<String> set = redisCommands.smembers(redisData.getKey());
                        ((RedisData<Set<String>>) redisData).setValue(set);
                        break;
                    case HASH:
                        Map<String, String> map = redisCommands.hgetall(redisData.getKey());
                        ((RedisData<Map<String, String>>) redisData).setValue(map);
                        break;
                    default:
                        break;
                }
            }
            controller.setData(redisData);
            controller.init(Context.getInstance());
            Scene detailScene = new Scene(content);
            detailScene.getStylesheets().add(DeatailController.class.getResource("/css/global.css").toExternalForm());
            detailStage.setScene(detailScene);
            detailStage.setTitle("Content Detail");
            StageUtils.quarterScreeStage(detailStage);
            detailStage.initModality(Modality.WINDOW_MODAL);
            detailStage.initOwner(Context.getInstance().getMainStage() );
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return detailStage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
