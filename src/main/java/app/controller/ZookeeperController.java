package app.controller;

import app.components.DetailDialog;
import app.config.Config;
import app.config.ZookeeperConfig;
import app.controller.common.TreeBaseController;
import app.data.TreeNode;
import app.data.zk.ZkData;
import app.data.zk.ZkDataType;
import app.scheduler.JavaFxScheduler;
import app.util.YamlUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
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
 * @date: 2020-04-08
 **/
public class ZookeeperController extends TreeBaseController<ZkData> implements Initializable {
    private static final Logger logger = LogManager.getLogger(ZookeeperController.class);
    private static final String SPLITTER = "/";
    private ZookeeperConfig defaultConfig = new ZookeeperConfig();
    private ZooKeeper zooKeeper;
    @FXML private Button importBtn;
    @FXML private TextField importPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TreeTableColumn<ZkData, String> keyColumn = new TreeTableColumn<>("PATH");
        keyColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ZkData, String> p) -> {
            ZkData zkData = p.getValue().getValue();
            if (StringUtils.isBlank(zkData.getCanonicalName())) {
                return new ReadOnlyStringWrapper(zkData.getName());
            } else {
                return new ReadOnlyStringWrapper(zkData.getCanonicalName());
            }
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
                private HBox hBox = new HBox();
                private FontIcon refreshIcon = new FontIcon(FontAwesome.REFRESH);
                private FontIcon deleteIcon = new FontIcon(FontAwesome.CLOSE);

                {
                    hBox.setAlignment(Pos.CENTER);
                    hBox.getChildren().addAll(refreshIcon, deleteIcon);
                }

                @Override
                protected void updateItem(ZkData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null) {
                        super.setText(null);
                        super.setGraphic(null);
                    } else if (item.getType() != null) {
                        refreshIcon.setOnMouseClicked(event -> {
                            loadTreeView(item.getCanonicalName());
                        });
                        deleteIcon.setOnMouseClicked(event -> {
                            deleteKey(item.getCanonicalName());
                            loadTreeView(null);
                        });
                        setGraphic(hBox);
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
            TreeTableRow<ZkData> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                TreeItem<ZkData> treeItem = ((TreeTableRow<ZkData>)event.getSource()).getTreeItem();
                if (treeItem == null) {
                    return;
                }
                ZkData data = treeItem.getValue();
                if (data != null && data.getValue() != null) {
                    try {
                        DetailDialog detailDialog = new DetailDialog(data);
                        detailDialog.showAndWait();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            });
            return row;
        });
        importBtn.setOnMouseClicked(this::importHandler);
        if (rootTreeNode == null) {
            rootTreeNode = buildTreeNode("/", true);
            rootTreeNode.setTreeItem(new TreeItem<>(rootTreeNode.getValue()));
        }
        keyValueTreeTableView.setRoot(rootTreeNode.getTreeItem());
    }

    @Override
    public Config loadDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public void loadTreeView(String reloadKey) {
        Observable.fromSupplier(() -> {
            if (zooKeeper != null
                    && zooKeeper.getState().isConnected()
                    && zooKeeper.getState().isAlive()) {
                return zooKeeper;
            }
            CountDownLatch connected = new CountDownLatch(1);
            ZookeeperConfig conf = defaultConfig;
            zooKeeper = new ZooKeeper(String.format("%s:%s", conf.getHost(), conf.getPort()), Integer.MAX_VALUE,
                    event -> {
                        logger.info("Receive watched event：{}", event);
                        if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                            connected.countDown();
                        }
                    }
            );
            connected.await();
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
            return zooKeeper;
        })
                .subscribeOn(Schedulers.single())
                .<TreeNode<ZkData>>flatMap(zooKeeper -> {
                    return Observable.create(emitter -> {
                        if (StringUtils.isBlank(reloadKey)) {
                            retrieveTreeNode("/", emitter);
                        } else {
                            emitter.onNext(buildTreeNode(reloadKey, false));
                        }
                        emitter.onComplete();
                    });
                })
                .buffer(3, TimeUnit.SECONDS)
                .<TreeNode<ZkData>>map(list -> {
                    TreeNode<ZkData> root = rootTreeNode;
                    if (StringUtils.isBlank(reloadKey)) {
                        root.getChildren().clear();
                    }
                    for (TreeNode<ZkData> treeNode : list) {
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
                    keyValueTreeTableView.refresh();
                });
        ;
    }

    @Override
    public void deleteKey(String key) {
        TreeNode<ZkData> treeNode = buildTreeNode(key, false);
        if (treeNode.getValue().getVersion() != null) {
            try {
                zooKeeper.delete(key, treeNode.getValue().getVersion());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public TreeNode<ZkData> buildTreeNode(String canonicalName, Boolean mocked) {
        TreeNode<ZkData> treeNode = new TreeNode<>();
        ZkData zkData = new ZkData();
        List<String> pieces = treeKeys(canonicalName, SPLITTER);
        zkData.setName(pieces.get(pieces.size() - 1));
        zkData.setCanonicalName(canonicalName);
        if (mocked) {
            zkData.setMocked(true);
            treeNode.setValue(zkData);
            return treeNode;
        }
        try {
            Stat stat = new Stat();
            byte[] nodeData = zooKeeper.getData(canonicalName, null, stat);
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
        treeNode.setValue(zkData);
        return treeNode;
    }

    private void retrieveTreeNode(String rootPath, ObservableEmitter<TreeNode<ZkData>> emitter) throws Exception {
        List<String> children = zooKeeper.getChildren(rootPath, null);
        for (String child : children) {
            String childPath = SPLITTER.equals(rootPath) ? rootPath + child : rootPath + SPLITTER + child;
            emitter.onNext(buildTreeNode(childPath, false));
            retrieveTreeNode(childPath, emitter);
        }
    }

    private void importHandler(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Yaml Files", "*.yml", "*.yaml"));
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            String path = importPath.getText();
            logger.info("path: {}, file: {}", path, selectedFile.getAbsolutePath());
            if (StringUtils.isBlank(path)) {
                return;
            }
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
    }
}
