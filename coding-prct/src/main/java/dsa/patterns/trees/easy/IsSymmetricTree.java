package dsa.patterns.trees.easy;

import dsa.patterns.trees.TreeNode;

public class IsSymmetricTree {

    static class Solution {
        public boolean isSymmetric(TreeNode root) {
            if (root == null) return true;

            return isMirror(root.left, root.right);
        }

        private boolean isMirror(TreeNode leftTree, TreeNode rightTree) {
            if (leftTree == null && rightTree == null) return true;
            if (leftTree == null || rightTree == null) return false;

            return (leftTree.val == rightTree.val) && isMirror(leftTree.left, rightTree.right) && isMirror(leftTree.right, rightTree.left);
        }
    }

}
