package dsa.patterns.binarytrees.medium;

import dsa.patterns.binarytrees.TreeNode;

// Use bottom up approach (dfs).
public class BinaryTreeTilt {

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
        public int findTilt(TreeNode root) {
            int tiltSum = 0;
            return findSum(root)[1];
        }

        private int[] findSum(TreeNode root) {
            if (root == null) return new int[] {0, 0}; // {sum, tilt}

            int[] left = findSum(root.left);
            int[] right = findSum(root.right);

            int currentTilt = Math.abs(left[0] - right[0]);
            // The total tilt is: left's tilt + right's tilt + current node's tilt
            int totalTilt = left[1] + right[1] + currentTilt;
            int totalSum = left[0] + right[0] + root.val;

            return new int[] {totalSum, totalTilt};
        }
    }

}
