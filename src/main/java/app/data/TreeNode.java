package app.data;

import javafx.scene.control.TreeItem;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author: pickjob@126.com
 * @date: 2020-06-22
 **/
public class TreeNode<T extends TreeData> implements Comparable<TreeNode> {
    private T value;
    private TreeItem<T> treeItem;
    private Set<TreeNode<T>> children = new TreeSet<>();

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public TreeItem<T> getTreeItem() {
        return treeItem;
    }

    public void setTreeItem(TreeItem<T> treeItem) {
        this.treeItem = treeItem;
    }

    public Set<TreeNode<T>> getChildren() {
        return children;
    }

    public void setChildren(Set<TreeNode<T>> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equals(value, treeNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "name=" + value.getName() +
                ", canonicalName=" + value.getCanonicalName() +
                ", children=" + children +
                '}';
    }

    @Override
    public int compareTo(TreeNode o) {
        return value.getCanonicalName().compareTo(o.getValue().getCanonicalName());
    }
}
