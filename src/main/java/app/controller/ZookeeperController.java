package app.controller;

import app.controller.common.BaseController;
import app.data.TreeNode;
import app.data.zk.ZkData;
import app.enums.LoginKeyEnum;
import app.scheduler.JavaFxScheduler;
import app.util.Constants;
import app.util.StageUtils;
import app.util.TreeNodeUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
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
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author: pickjob@126.com
 * @time: 2020-04-08
 **/
public class ZookeeperController extends BaseController {
    private static final Logger logger = LogManager.getLogger(ZookeeperController.class);
    private static final String SPLITTER = "/";
    private EnumMap<LoginKeyEnum, String> loginEnumMap;
    private volatile boolean isLoad = false;
    private TreeNode<ZkData> rootZkTreeNode;
    private ZooKeeper zooKeeper;
    @FXML private TreeTableView<ZkData> keyValueTreeTableView;

    @Override
    public void buildUIComponents() {
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
        TreeTableColumn<ZkData, String> versionColumn = new TreeTableColumn<>("VERSION");
        versionColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ZkData, String> p) -> {
            ZkData redisData = p.getValue().getValue();
            return new ReadOnlyStringWrapper(redisData.getVersion() == null ? null : redisData.getVersion() + "");
        });
        TreeTableColumn<ZkData, ZkData> operatorColumn = new TreeTableColumn<>("operator");
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
                private Button refreshBtn = new Button();
                @Override
                protected void updateItem(ZkData item, boolean empty) {
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
        keyColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(3).subtract(1));
        valueColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(3).subtract(1));
        versionColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        operatorColumn.prefWidthProperty().bind(keyValueTreeTableView.widthProperty().divide(10).multiply(2).subtract(1));
        keyValueTreeTableView.getColumns().addAll(keyColumn, valueColumn, versionColumn, operatorColumn);
        keyValueTreeTableView.setRowFactory(treeTableView -> {
            TreeTableRow<ZkData> row = new TreeTableRow();
            row.setOnMouseClicked(event -> {
                ZkData data = row.getTreeItem().getValue();
                if (data != null) {
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
            if (rootZkTreeNode == null) {
                rootZkTreeNode = new TreeNode<>("Root", null);
                TreeItem<ZkData> rootTreeItem = new TreeItem<>(new ZkData());
                rootZkTreeNode.setTreeItem(rootTreeItem);
                keyValueTreeTableView.setRoot(rootZkTreeNode.getTreeItem());
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
            String host = loginEnumMap.get(LoginKeyEnum.HOST);
            String port = loginEnumMap.get(LoginKeyEnum.PORT);

            Observable.<ZooKeeper>fromSupplier(() -> {
                if (zooKeeper != null) {
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
                for (TreeNode<ZkData> treeNode: list) {
                    TreeNodeUtils.appendOrReplaceTreeNode(rootZkTreeNode, treeNode, SPLITTER, ZkData::new);
                }
                return rootZkTreeNode;
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

    private void retrieveTreeNode(String rootPath, ObservableEmitter<TreeNode<ZkData>> emitter) throws Exception {
        List<String> children = zooKeeper.getChildren(rootPath, null);
        for (String child : children) {
            String childPath = SPLITTER.equals(rootPath) ? rootPath + child : rootPath + SPLITTER + child;
            emitter.onNext(TreeNodeUtils.buildTreeNode(childPath, buildData(childPath), SPLITTER));
            retrieveTreeNode(childPath, emitter);
        }
    }

    private ZkData buildData(String path) {
        ZkData zkData = new ZkData();
        try {
            byte[] nodeData = zooKeeper.getData(path, null, null);
            if (nodeData != null) {
                zkData.setValue(new String(nodeData));
            }
            Stat stat = zooKeeper.exists(path, null);
            zkData.setVersion(stat.getVersion());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return zkData;
    }
}
