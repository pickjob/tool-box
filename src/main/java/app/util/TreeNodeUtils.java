package app.util;

import app.data.TreeData;
import app.data.TreeNode;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author: pickjob@126.com
 * @time: 2020-06-22
 **/
public class TreeNodeUtils {
    private static final Logger logger = LogManager.getLogger(TreeNodeUtils.class);

    public static <T extends TreeData> TreeNode<T> buildTreeNode(String canonicalName, T data, String splitter) {
        TreeNode<T> treeNode = new TreeNode<>();
        List<String> pieces = treeKeys(canonicalName, splitter);
        treeNode.setName(pieces.get(pieces.size() - 1));
        treeNode.setCanonicalName(canonicalName);
        data.setName(treeNode.getName());
        data.setCanonicalName(treeNode.getCanonicalName());
        treeNode.setValue(data);
        TreeItem<T> treeItem = new TreeItem<>(data);
        treeNode.setTreeItem(treeItem);
        return treeNode;
    }

    public static <T extends TreeData> void appendOrReplaceTreeNode(TreeNode<T> rootTreeNode, TreeNode<T> treeNode, String splitter, Supplier<T> emptyDataSupplier) {
        List<String> treeKeys = treeKeys(treeNode.getCanonicalName(), splitter);
        TreeNode<T> curNode = rootTreeNode;
        for (String key : treeKeys) {
            Optional<TreeNode<T>> node = curNode.getChildren()
                                                .stream()
                                                .filter(n -> {
                                                    return n.getName().equals(key);
                                                })
                                                .findFirst();
            if (node.isPresent()) {
                if (node.get().getCanonicalName().equals(treeNode.getCanonicalName())) {
                    TreeNode<T> existNode = node.get();
                    existNode.getTreeItem().getChildren().clear();
                    existNode.setTreeItem(treeNode.getTreeItem());
                    existNode.setValue(treeNode.getValue());
                    return;
                }
                curNode = node.get();
            } else {
                String canonicalName = null;
                if (curNode.getCanonicalName() != null) {
                    if (curNode.getCanonicalName().endsWith(splitter)) {
                        canonicalName = curNode.getCanonicalName() + key;
                    } else {
                        canonicalName = curNode.getCanonicalName() + splitter + key;
                    }
                } else {
                    canonicalName = key;
                }
                if (canonicalName.equals(treeNode.getCanonicalName())) {
                    curNode.getChildren().add(treeNode);
                    curNode = treeNode;
                } else {
                    T emptyData = emptyDataSupplier.get();
                    emptyData.setName(key);
                    emptyData.setCanonicalName(canonicalName);
                    TreeNode<T> emptyNode = buildTreeNode(canonicalName, (T) emptyData, splitter);
                    curNode.getChildren().add(emptyNode);
                    curNode = emptyNode;
                }
            }
        }
    }

    public static <T extends TreeData> void buildTreeItem(TreeNode<T> rootTreeNode) {
        TreeItem<T> rootTreeItem = rootTreeNode.getTreeItem();
        rootTreeItem.setExpanded(true);
        rootTreeItem.getChildren().clear();
        for (TreeNode<T> treeNode : rootTreeNode.getChildren()) {
            TreeItem<T> treeItem = treeNode.getTreeItem();
            rootTreeItem.getChildren().add(treeItem);
            buildTreeItem(treeNode);
        }
    }

    public static <T extends TreeData> void iteratorTreeNode(TreeNode<T> rootTreeNode, Consumer<TreeNode<T>> consumer) {
        if (consumer != null) {
            consumer.accept(rootTreeNode);
        }
        for (TreeNode<T> child : rootTreeNode.getChildren()) {
            iteratorTreeNode(child, consumer);
        }
    }

    private static List<String> treeKeys(String canonicalName, String splitter) {
        List<String> result = new ArrayList<>();
        if (canonicalName.equals(splitter)) {
            result.add(splitter);
            return result;
        }
        String[] pieces = canonicalName.split(splitter);
        for (String piece : pieces) {
            if (StringUtils.isBlank(piece)) {
                result.add(splitter);
            } else {
                result.add(piece);
            }
        }
        return result;
    }
}
