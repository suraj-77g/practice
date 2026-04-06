package dsa.patterns.trees.medium;

import dsa.patterns.trees.TreeNode;

// Can also use BFS - optimal approach
// In DFS - take care of the leaf node (don't treat cases where only 1 child is null as leaf node).
public class MinDepth {

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
        public int minDepth(TreeNode root) {
            if (root == null) return 0;

            int leftDepth = minDepth(root.left);
            int rightDepth = minDepth(root.right);

            if (leftDepth == 0) return rightDepth + 1;
            if (rightDepth == 0) return leftDepth + 1;

            return Math.min(leftDepth, rightDepth) + 1;
        }

    }

}