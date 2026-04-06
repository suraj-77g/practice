package dsa.patterns.trees.easy;

import dsa.patterns.trees.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LevelOrderTraversal {

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
        public List<List<Integer>> levelOrder(TreeNode root) {
            List<List<Integer>> results = new ArrayList<>();
            if (root == null) return results;

            Queue<TreeNode> queue = new LinkedList<>();
            queue.add(root);

            while (!queue.isEmpty()) {
                int size = queue.size();
                List<Integer> currLevel = new ArrayList<>();

                for (int i = 0; i < size; i++) {
                    TreeNode node = queue.poll();

                    if (node.left != null)
                        queue.add(node.left);
                    if (node.right != null)
                        queue.add(node.right);

                    currLevel.add(node.val);
                }
                results.add(currLevel);
            }
            return results;
        }
    }

}
