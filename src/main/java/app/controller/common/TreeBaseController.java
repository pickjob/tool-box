package app.controller.common;

import app.config.Config;
import app.data.TreeData;
import app.data.TreeNode;
import app.enums.LoginKey;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * @author pickjob@126.com
 * @date 2019-05-16
 **/
public abstract class TreeBaseController<T extends TreeData> extends BaseController {
    private static final Logger logger = LogManager.getLogger(TreeBaseController.class);
    protected Config defaultConfig;
    protected TreeNode<T> rootTreeNode;
    @FXML protected TreeTableView<T> keyValueTreeTableView;

    @Override
    public boolean isNeedLogin() {
        return true;
    }

    @Override
    public abstract Config loadDefaultConfig();

    @Override
    public void loadWithLoginEnumMap(EnumMap<LoginKey, String> loginEnumMap) {
        loadDefaultConfig().initWithLoginEnumMap(loginEnumMap);
        loadDefaultConfig().writeLoginEnumMapToFile();
        loadTreeView(null);
    }

    public abstract void loadTreeView(String reloadKey);

    public abstract void deleteKey(String key);

    public abstract TreeNode<T> buildTreeNode(String canonicalName, Boolean mocked);

    protected void appendOrReplaceTreeNode(TreeNode<T> root, TreeNode<T> treeNode, String splitter) {
        T data = treeNode.getValue();
        List<String> treeKeys = treeKeys(data.getCanonicalName(), splitter);
        TreeNode<T> curNode = root;
        for (String key : treeKeys) {
            Optional<TreeNode<T>> node = curNode.getChildren()
                    .stream()
                    .filter(n -> {
                        return n.getValue().getName().equals(key);
                    })
                    .findFirst();
            if (node.isPresent()) {
                if (node.get().getValue().getCanonicalName().equals(treeNode.getValue().getCanonicalName())) {
                    TreeNode<T> existNode = node.get();
                    existNode.setValue(treeNode.getValue());
                    existNode.setTreeItem(null);
                    return;
                }
                curNode = node.get();
            } else {
                String canonicalName = null;
                if (StringUtils.isNotBlank(curNode.getValue().getCanonicalName())) {
                    if (curNode.getValue().getCanonicalName().endsWith(splitter)) {
                        canonicalName = curNode.getValue().getCanonicalName() + key;
                    } else {
                        canonicalName = curNode.getValue().getCanonicalName() + splitter + key;
                    }
                } else {
                    canonicalName = key;
                }
                if (canonicalName.equals(treeNode.getValue().getCanonicalName())) {
                    curNode.getChildren().add(treeNode);
                    curNode = treeNode;
                } else {
                    TreeNode<T> n = buildTreeNode(canonicalName, true);
                    curNode.getChildren().add(n);
                    curNode = n;
                }
            }
        }
    }

    protected void filter(TreeNode<T> root, Function<String, Boolean> searchFunction, Set<TreeNode<T>> showSet, Set<TreeNode<T>> hideSet) {
        for (TreeNode<T> node : root.getChildren()) {
            if (searchBackTracing(node, searchFunction, showSet, hideSet)) {
                filter(node, searchFunction, showSet, hideSet);
            } else {
                root.getTreeItem().getChildren().remove(node.getTreeItem());
            }
        }
    }

    protected List<String> treeKeys(String canonicalName, String splitter) {
        List<String> result = new ArrayList<>();
        if (canonicalName.equals(splitter)) {
            result.add(splitter);
            return result;
        }
        String[] pieces = canonicalName.split(splitter);
        for (String piece : pieces) {
            if (StringUtils.isNotBlank(piece)) {
                result.add(piece);
            }
        }
        return result;
    }

    protected void buildTreeItem(TreeNode<T> treeNode, String reloadKey) {
        TreeItem<T> treeItem = treeNode.getTreeItem();
        if (treeItem == null) {
            treeItem = new TreeItem<>(treeNode.getValue());
            treeNode.setTreeItem(treeItem);
            treeItem.setExpanded(false);
        }
        if (StringUtils.isNotBlank(reloadKey)) {
            if (StringUtils.isBlank(treeNode.getValue().getCanonicalName())
                    || reloadKey.startsWith(treeNode.getValue().getCanonicalName())) {
                treeItem.getChildren().clear();
            }
        }
        for (TreeNode<T> node : treeNode.getChildren()) {
            buildTreeItem(node, reloadKey);
            treeItem.getChildren().remove(node.getTreeItem());
            treeItem.getChildren().add(node.getTreeItem());
        }
    }

    protected void selectedOneBackTracing(TreeNode<T> node, String reloadKey) {
        if (StringUtils.isBlank(reloadKey)) {
            return;
        }
        if (reloadKey.equals(node.getValue().getCanonicalName())) {
            keyValueTreeTableView.getSelectionModel().select(node.getTreeItem());
        }
        for (TreeNode<T> n : node.getChildren()) {
            selectedOneBackTracing(n, reloadKey);
        }
    }

    private boolean searchBackTracing(TreeNode<T> node, Function<String, Boolean> searchFunction,Set<TreeNode<T>> showSet, Set<TreeNode<T>> hideSet) {
        if (hideSet.contains(node)) {
            return false;
        }
        if (showSet.contains(node)) {
            return true;
        }
        if (searchFunction.apply(node.getValue().getCanonicalName())) {
            showSet.add(node);
            return true;
        }
        for (TreeNode<T> n : node.getChildren()) {
            if (searchBackTracing(n, searchFunction, showSet, hideSet)) {
                return true;
            }
        }
        hideSet.add(node);
        return false;
    }
}
