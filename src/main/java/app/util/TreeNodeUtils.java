package app.util;

import app.data.TreeNode;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author: pickjob@126.com
 * @time: 2020-06-22
 **/
public class TreeNodeUtils {
    private static final Logger logger = LogManager.getLogger(TreeNodeUtils.class);

    public static <T> void iteratorTreeNode(TreeNode<T> rootTreeNode, Consumer<TreeNode<T>> consumer) {
        if (consumer != null) {
            consumer.accept(rootTreeNode);
        }
        for (TreeNode<T> child : rootTreeNode.getChildren()) {
            iteratorTreeNode(child, consumer);
        }
    }

    public static <T> void buildTreeItemRelation(TreeNode<T> rootTreeNode, String reloadKey) {
        TreeItem<T> rootTreeItem = rootTreeNode.getTreeItem();
        if (rootTreeItem == null) {
            rootTreeItem = new TreeItem<>(rootTreeNode.getValue());
            rootTreeNode.setTreeItem(rootTreeItem);
        }
        rootTreeItem.setExpanded(true);
        for (TreeNode<T> treeNode : rootTreeNode.getChildren()) {
            TreeItem<T> treeItem = treeNode.getTreeItem();
            if (treeItem == null) {
                treeItem = new TreeItem<>(treeNode.getValue());
                treeNode.setTreeItem(treeItem);
                rootTreeItem.getChildren().add(treeItem);
            }
            if (StringUtils.isNotBlank(reloadKey) && reloadKey.equals(treeNode.getCanonicalName())) {
                treeItem.setValue(treeNode.getValue());
            }
            buildTreeItemRelation(treeNode, reloadKey);
        }
    }

    public static <T> void appendKeyToTreeNode(TreeNode<T> rootTreeNode, String key, String splitor) {
        String[] keyPieces = key.split(splitor);
        StringJoiner stringJoiner = new StringJoiner(splitor);
        Set<TreeNode<T>> children = rootTreeNode.getChildren();
        for (String piece : keyPieces) {
            stringJoiner.add(piece);
            TreeNode<T> treeNode = new TreeNode<>(piece, stringJoiner.toString());
            Optional<TreeNode<T>> optionalTreeNode = children.stream().filter(node -> node.equals(treeNode)).findFirst();
            if (optionalTreeNode.isPresent()) {
                children = optionalTreeNode.get().getChildren();
            } else {
                children.add(treeNode);
                children = treeNode.getChildren();
            }
        }
    }

    public static <T> TreeNode<T> retriveTreeNodeByCanonicalName(TreeNode<T> rootTreeNode, String canonicalName) {
        if (StringUtils.isNotBlank(canonicalName)) {
            return null;
        }
        AtomicReference<TreeNode<T>> result = new AtomicReference<>();
        iteratorTreeNode(rootTreeNode, treeNode -> {
            if (canonicalName.equals(treeNode.getCanonicalName())) {
                result.set(treeNode);
            }
        });
        return result.get();
    }
}
