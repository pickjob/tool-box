package app.util.splitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * @author: pickjob@126.com
 * @time: 2020-06-22
 **/
public class TreeNodeUtils {
    private static final Logger logger = LogManager.getLogger(TreeNodeUtils.class);

    public static TreeNode buildTree(List<String> keys, String splitor) {
        return buildTree(keys, splitor, null);
    }

    public static <T>TreeNode buildTree(List<String> keys, String splitor, Function<String, T> function) {
        TreeNode<T> root = new TreeNode<T>("Root", null);
        for (String key : keys) {
            String[] keyPieces = key.split(splitor);
            StringJoiner stringJoiner = new StringJoiner(splitor);
            Set<TreeNode<T>> children = root.getChildren();
            for (int i = 0; i < keyPieces.length; i++) {
                String piece = keyPieces[i];
                stringJoiner.add(piece);
                TreeNode<T> node = new TreeNode<>(piece, stringJoiner.toString());
                if (!children.contains(node)) {
                    children.add(node);
                }
                for (TreeNode n : children) {
                    if (n.equals(node)) node = n;
                }
                if (function != null) {
                    node.setData(function.apply(node.getFullPath()));
                }
                children = node.getChildren();
            }
        }
        if (function != null) {
            root.setData(function.apply(root.getFullPath()));
        }
        return root;
    }
}
