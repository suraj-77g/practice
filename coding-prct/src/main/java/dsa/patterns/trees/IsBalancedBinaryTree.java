package dsa.patterns.trees;

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

}
