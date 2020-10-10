package app.controller.common;

import app.data.TreeNode;
import app.enums.LoginKeyEnum;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;

/**
 * @author pickjob@126.com
 * @time 2019-05-16
 **/
public abstract class TreeBaseController<T> extends BaseController {
    private static final Logger logger = LogManager.getLogger(TreeBaseController.class);
    private volatile boolean isLoad = false;
    protected EnumMap<LoginKeyEnum, String> loginEnumMap;
    protected TreeNode<T> rootTreeNode;
    @FXML protected TreeTableView<T> keyValueTreeTableView;

    @Override
    public void runWith(Object arg) {
        if (!isLoad) {
            if (rootTreeNode == null) {
                rootTreeNode = new TreeNode<>("Root", null);
                TreeItem<T> rootTreeItem = new TreeItem<>(buildData(null));
                rootTreeNode.setTreeItem(rootTreeItem);
                keyValueTreeTableView.setRoot(rootTreeNode.getTreeItem());
            }
            if (arg != null && arg instanceof EnumMap) {
                loginEnumMap = (EnumMap<LoginKeyEnum, String>) arg;
                loadTreeView(null);
            }
        }
    }

    @Override
    public boolean isNeedLogin() {
        return true;
    }

    protected abstract void loadTreeView(String reloadKey);

    protected abstract void deleteKey(String key);

    protected abstract T buildData(String key);

    protected void finishLoad() {
        isLoad = true;
    }
}
