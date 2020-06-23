package app.util.splitor;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author: pickjob@126.com
 * @time: 2020-06-22
 **/
public class TreeNode<T> implements Comparable<TreeNode<T>> {
    private String name;
    private String fullPath;
    private Set<TreeNode<T>> children;
    private T data;

    public TreeNode(String name, String fullPath) {
        this.name = name;
        this.fullPath = fullPath;
        this.children = new TreeSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Set<TreeNode<T>> getChildren() {
        return children;
    }

    public void setChildren(Set<TreeNode<T>> children) {
        this.children = children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode that = (TreeNode) o;
        return fullPath.equals(that.fullPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullPath);
    }

    @Override
    public int compareTo(TreeNode<T> o) {
        return this.fullPath.compareTo(o.getFullPath());
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "name='" + name + '\'' +
                ", fullPath='" + fullPath + '\'' +
                ", children=" + children +
                ", data=" + data +
                '}';
    }
}
