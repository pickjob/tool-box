package app.controller;

import app.controller.common.BaseController;
import app.controller.common.TreeBaseController;
import app.data.TreeNode;
import app.data.zk.ZkData;
import app.data.zk.ZkDataType;
import app.enums.LoginKeyEnum;
import app.scheduler.JavaFxScheduler;
import app.util.Constants;
import app.util.StageUtils;
import app.util.TreeNodeUtils;
import app.util.YamlUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.EphemeralType;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-08
 **/
public class ZookeeperController extends TreeBaseController<ZkData> implements Initializable {
    private static final Logger logger = LogManager.getLogger(ZookeeperController.class);
    private static final String SPLITTER = "/";
    private ZooKeeper zooKeeper;
    @FXML private Button importBtn;
    @FXML private TextField importPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeTableColumn<ZkData, String> keyColumn = new TreeTableColumn<>("PATH");
        keyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ZkData, String> p) -> {
            ZkData zkData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(zkData.getName());
        });
        TreeTableColumn<ZkData, String> valueColumn = new TreeTableColumn<>("VALUE");
        valueColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ZkData, String> p) -> {
            ZkData redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getValue());
        });
        valueColumn.setCellFactory((TreeTableColumn<ZkData, String> column) -> {
            return new TreeTableCell<ZkData, String>() {
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
        TreeTableColumn<ZkData, String> typeColumn = new TreeTableColumn<>("TYPE");
        typeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ZkData, String> p) -> {
            ZkData redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getType() == null ? null : redisData.getType().name());
        });
        TreeTableColumn<ZkData, ZkData> operatorColumn = new TreeTableColumn<>("OPERATOR");
        operatorColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ZkData, ZkData> p) -> {
            ZkData zkData = p.getValue().getValue();
            return new ObjectBinding<ZkData>() {
                @Override
                protected ZkData computeValue() {
                    return zkData;
                }
            };
        });
        operatorColumn.setCellFactory((TreeTableColumn<ZkData, ZkData> column) -> {
            return new TreeTableCell<ZkData, ZkData>() {
                private HBox hBox = null;
                private Button refreshBtn = new Button();
                private Button deleteBtn = new Button();

                @Override
                protected void updateItem(ZkData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null) {
                        super.setText(null);
                        super.setGraphic(null);
                    } else if (item.getType() != null) {
                        if (hBox == null) {
                            FontIcon refreshIcon = new FontIcon(FontAwesome.REFRESH);
                            refreshIcon.setIconSize(10);
                            refreshBtn.setGraphic(refreshIcon);
                            refreshBtn.setOnMouseClicked(event -> {
                                loadTreeView(item.getCanonicalName());
                            });
                            FontIcon delIcon = new FontIcon(FontAwesome.CLOSE);
                            delIcon.setIconSize(10);
                            deleteBtn.setGraphic(delIcon);
                            deleteBtn.setOnMouseClicked(event -> {
                                deleteKey(item.getCanonicalName());
                            });
                            hBox = new HBox();
                            hBox.getChildren().addAll(refreshBtn, deleteBtn);
                        }
                        if (StringUtils.isNotBlank(item.getValue())) {
                            setGraphic(hBox);
                        }
                    }
                }
            };
        });
        keyColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(3).subtract(1));
        valueColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(3).subtract(1));
        typeColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        operatorColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        keyValueTreeTableView.getColumns().addAll(keyColumn, valueColumn, typeColumn, operatorColumn);
        keyValueTreeTableView.setRowFactory(treeTableView -> {
            TreeTableRow<ZkData> row = new TreeTableRow();
            row.setOnMouseClicked(event -> {
                TreeItem<ZkData> treeItem = ((TreeTableRow<ZkData>)event.getSource()).getTreeItem();
                if (treeItem == null) {
                    return;
                }
                ZkData data = treeItem.getValue();
                if (data != null && StringUtils.isNotBlank(data.getValue())) {
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/fxml/common/detail.fxml"));
                        Parent content = loader.load();
                        final BaseController controller = loader.getController();
                        controller.runWith(data);
                        Scene detailScene = new Scene(content);
                        detailScene.getStylesheets().addAll(Constants.loadStyleSheets());
                        Stage detailStage = new Stage();
                        detailStage.setScene(detailScene);
                        detailStage.setTitle("Zookeeper Detail");
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
        importBtn.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Yaml Files", "*.yml", "*.yaml"));
            fileChooser.setInitialDirectory(new File("D:\\code\\personal\\spring-cloud-starter\\spring-cloud-gateway\\src\\main\\resources"));
            File selectedFile = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());
            if (selectedFile != null) {
                String path = importPath.getText();
                logger.info("path: {}, file: {}", path, selectedFile.getAbsolutePath());
                if (StringUtils.isBlank(path)) return;
                Map<String, String> configMap = YamlUtils.covertYamlToProperties(selectedFile);
                if (zooKeeper != null
                        && zooKeeper.getState().isConnected()
                        && zooKeeper.getState().isAlive()) {
                    try {
                        String[] pieces = path.split("/");
                        String parent = "";
                        for (String piece : pieces) {
                            if (StringUtils.isBlank(piece)) continue;
                            parent += "/" + piece;
                            Stat stat = zooKeeper.exists(parent, null);
                            if (stat == null) {
                                zooKeeper.create(parent, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                            }
                        }
                        for (String key : configMap.keySet()) {
                            String val = configMap.get(key);
                            Stat stat = zooKeeper.exists(path + "/" + key, null);
                            if (stat == null) {
                                zooKeeper.create(path + "/" + key, val.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                            } else {
                                zooKeeper.setData(path + "/" + key, val.getBytes(), stat.getVersion());
                            }
                        }
                        loadTreeView(null);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    @Override
    protected void loadTreeView(String reloadKey) {
        String host = loginEnumMap.get(LoginKeyEnum.HOST);
        String port = loginEnumMap.get(LoginKeyEnum.PORT);

        Observable.<ZooKeeper>fromSupplier(() -> {
            if (zooKeeper != null
                    && zooKeeper.getState().isConnected()
                    && zooKeeper.getState().isAlive()) {
                return zooKeeper;
            }
            CountDownLatch connected = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(String.format("%s:%s", host, port), Integer.MAX_VALUE,
                    event -> {
                        logger.info("Receive watched event：{}", event);
                        if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                            connected.countDown();
                        }
                    }
            );
            connected.await();
            return zooKeeper;
        })
                .subscribeOn(Schedulers.single())
                .<TreeNode<ZkData>>flatMap(zooKeeper -> {
                    return Observable.<TreeNode<ZkData>>create(emitter -> {
                        if (StringUtils.isBlank(reloadKey)) {
                            retrieveTreeNode("/", emitter);
                        } else {
                            emitter.onNext(TreeNodeUtils.buildTreeNode(reloadKey, buildData(reloadKey), SPLITTER));
                        }
                        emitter.onComplete();
                    });
                })
                .buffer(3, TimeUnit.SECONDS)
                .<TreeNode<ZkData>>map(list -> {
                    for (TreeNode<ZkData> treeNode : list) {
                        TreeNodeUtils.appendOrReplaceTreeNode(rootTreeNode, treeNode, SPLITTER, ZkData::new);
                    }
                    return rootTreeNode;
                })
                .observeOn(JavaFxScheduler.platform())
                .subscribe(rootTreeNode -> {
                    TreeNodeUtils.buildTreeItem(rootTreeNode);
                    keyValueTreeTableView.getScene()
                            .getWindow()
                            .setOnCloseRequest(windowEvent -> {
                        logger.info("closing ...");
                        if (zooKeeper != null
                                && zooKeeper.getState().isConnected()
                                && zooKeeper.getState().isAlive()) {
                            try {
                                zooKeeper.close();
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    });
                    keyValueTreeTableView.refresh();
                    finishLoad();
                });
        ;
    }

    @Override
    protected void deleteKey(String key) {
        ZkData zkData = buildData(key);
        if (zkData.getVersion() != null) {
            try {
                zooKeeper.delete(key, zkData.getVersion());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        rootTreeNode = new TreeNode<>("Root", null);
        TreeItem<ZkData> rootTreeItem = new TreeItem<>(buildData(null));
        rootTreeNode.setTreeItem(rootTreeItem);
        keyValueTreeTableView.setRoot(rootTreeNode.getTreeItem());
        loadTreeView(null);
    }

    private void retrieveTreeNode(String rootPath, ObservableEmitter<TreeNode<ZkData>> emitter) throws Exception {
        List<String> children = zooKeeper.getChildren(rootPath, null);
        for (String child : children) {
            String childPath = SPLITTER.equals(rootPath) ? rootPath + child : rootPath + SPLITTER + child;
            emitter.onNext(TreeNodeUtils.buildTreeNode(childPath, buildData(childPath), SPLITTER));
            retrieveTreeNode(childPath, emitter);
        }
    }

    @Override
    protected ZkData buildData(String path) {
        ZkData zkData = new ZkData();
        if (StringUtils.isBlank(path)) {
            return zkData;
        }
        TreeNodeUtils.buildTreeNode(path, zkData, SPLITTER);
        try {
            Stat stat = new Stat();
            byte[] nodeData = zooKeeper.getData(path, null, stat);
            if (nodeData != null) {
                zkData.setValue(new String(nodeData));
            }
            EphemeralType type = EphemeralType.get(stat.getEphemeralOwner());
            if (type == EphemeralType.VOID) {
                zkData.setType(ZkDataType.PERSISTENT);
            } else if (type == EphemeralType.NORMAL) {
                zkData.setType(ZkDataType.EPHEMERAL);
            } else if (type == EphemeralType.CONTAINER) {
                zkData.setType(ZkDataType.CONTAINER);
            } else if (type == EphemeralType.TTL) {
                zkData.setType(ZkDataType.TTL);
            }
            zkData.setVersion(stat.getVersion());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return zkData;
    }
}
