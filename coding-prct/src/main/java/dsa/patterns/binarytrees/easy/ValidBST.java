package dsa.patterns.binarytrees.easy;

import dsa.patterns.binarytrees.TreeNode;

// To determine whether the subtree rooted at the current node is a BST or not,
// we need to know the range (min, max value) the current node value is allowed to be in.
public class ValidBST {

    private static boolean dfs(TreeNode root, long min, long max) {
        // empty nodes are always valid
        if (root == null) return true;
        if (!(min < root.val && root.val < max)) {
            return false;
        }
        return dfs(root.left, min, root.val) && dfs(root.right, root.val, max);
    }

    public static boolean validBst(TreeNode root) {
        // root is always valid
        return dfs(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

}
