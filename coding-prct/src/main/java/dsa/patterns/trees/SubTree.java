package dsa.patterns.trees;

/*
    Given two binary trees root and subRoot, determine if subRoot is a subtree of root.
    A subtree of a binary tree is a tree that consists of a node in the tree and all of its descendants.
    An empty tree is considered a subtree of any tree (including another empty tree).
 */
public class SubTree {

    /**
     * Definition for a binary tree node.
     * public class TreeNode {
     *     int val;
     *     TreeNode left;
     *     TreeNode right;
     *     TreeNode() {}
     *     TreeNode(int val) { this.val = val; }
     *     TreeNode(int val, TreeNode left, TreeNode right) {
     *         this.val = val;
     *         this.left = left;
     *         this.right = right;
     *     }
     * }
     */
    static class Solution {

        public boolean isSubtree(TreeNode root, TreeNode subRoot) {
            // If the main tree is empty, subRoot cannot be a subtree
            if (root == null) return false;

            // 1. Check if the trees starting at these nodes are identical
            if (isSameTree(root, subRoot)) return true;

            // 2. If not, look for the subRoot in the left and right children
            return isSubtree(root.left, subRoot) || isSubtree(root.right, subRoot);
        }

        // Helper function to check if two trees are exactly the same
        private boolean isSameTree(TreeNode p, TreeNode q) {
            if (p == null && q == null) return true;
            if (p == null || q == null) return false;
            if (p.val != q.val) return false;

            return isSameTree(p.left, q.left) && isSameTree(p.right, q.right);
        }

    }

}
