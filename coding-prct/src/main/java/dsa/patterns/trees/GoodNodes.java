package dsa.patterns.trees;

// Given a binary tree root, a node X in the tree is named good if in the path from root to X there are no nodes with a value greater than X.
public class GoodNodes {

    static class Solution {
        public int goodNodes(TreeNode root) {
            return getGoodNodes(root.val, root);
        }

        private int getGoodNodes(int maxNodeOnPath, TreeNode root) {
            if (root == null) return 0;

            int count = 0;
            // 1. Check if current node is "Good"
            if (root.val >= maxNodeOnPath) {
                count = 1;
                maxNodeOnPath = root.val; // Update max for children
            }

            count += getGoodNodes(maxNodeOnPath, root.left);
            count += getGoodNodes(maxNodeOnPath, root.right);
            return count;
        }
    }

}
