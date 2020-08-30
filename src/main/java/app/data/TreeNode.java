package app.data;

import javafx.scene.control.TreeItem;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author: pickjob@126.com
 * @time: 2020-06-22
 **/
public class TreeNode<T> implements Comparable<TreeNode<T>> {
    private String name;
    private String canonicalName;
    private T value;
    private TreeItem<T> treeItem;
    private Set<TreeNode<T>> children;

    public TreeNode() {
        this.children = new TreeSet<>();
    }

    public TreeNode(String name, String canonicalName) {
        this();
        this.name = name;
        this.canonicalName = canonicalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

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
    public int compareTo(TreeNode<T> another) {
        return this.getCanonicalName().compareTo(another.getCanonicalName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return canonicalName.equals(treeNode.canonicalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canonicalName);
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "name='" + name + '\'' +
                ", canonicalName='" + canonicalName + '\'' +
                ", value=" + value +
                ", treeItem=" + treeItem +
                ", children=" + children +
                '}';
    }
}
