package dsa.patterns.trees.medium;

import dsa.patterns.trees.TreeNode;

/*
    Start at the leaves and work upward.
    At each node, get both the balance status and height from both children.
    If either child reports an unbalanced subtree, the current subtree is also unbalanced.
    If both children are balanced, compare their heights.
    A difference greater than 1 means the current subtree fails the balance requirement.
    Otherwise, the subtree is balanced and its height is max(left_height, right_height) + 1.
 */
public class IsBalancedBinaryTree {

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

        public boolean isBalanced(TreeNode root) {
            return checkHeightAndBalanced(root)[0] == 1;
        }

        public int[] checkHeightAndBalanced(TreeNode root) {
            if (root == null) return new int[] {1, 0};

            int[] left = checkHeightAndBalanced(root.left);
            if (left[0] == 0) return new int[] {0, -1};
            int[] right = checkHeightAndBalanced(root.right);
            if (right[0] == 0) return new int[] {0, -1};

            if (Math.abs(left[1] - right[1]) <= 1) {
                return new int[] {1, Math.max(left[1], right[1]) + 1};
            }
            return new int[] {0, -1};
        }
    }



    public boolean isBalancedV2(TreeNode root) {
        return getHeight(root) != -1;
    }

    private int getHeight(TreeNode root) {
        if (root == null) return 0;

        int leftHeight = getHeight(root.left);
        // If left subtree is unbalanced, pass the error up immediately
        if (leftHeight == -1) return -1;

        int rightHeight = getHeight(root.right);
        // If right subtree is unbalanced, pass the error up immediately
        if (rightHeight == -1) return -1;

        // If the current node is unbalanced
        if (Math.abs(leftHeight - rightHeight) > 1) return -1;

        // Otherwise, return the actual height
        return Math.max(leftHeight, rightHeight) + 1;
    }

}
