package dsa.patterns.binarytrees.medium;

import dsa.patterns.binarytrees.TreeNode;

// Trick: Use bottom-up approach (similar to IsBalancedBinaryTree)
public class Diameter {

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
        public int diameterOfBinaryTree(TreeNode root) {
            if (root == null) return 0;

            return getDiameter(root)[0];
        }

        private int[] getDiameter(TreeNode root) {
            if (root == null) return new int [] {0, 0};

            int[] leftResult = getDiameter(root.left);
            int leftHeight = leftResult[1];
            int leftDiameter = leftResult[0];

            int[] rightResult = getDiameter(root.right);
            int rightHeight = rightResult[1];
            int rightDiameter = rightResult[0];

            int currentDiameter = leftHeight + rightHeight;

            return new int [] {Math.max(currentDiameter, Math.max(leftDiameter, rightDiameter)), Math.max(leftHeight, rightHeight) + 1};
        }

    }

}
