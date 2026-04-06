package dsa.patterns.trees.bfs;

import dsa.patterns.trees.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BinaryTreeLevelOrderTraversal2 {

    static class Solution {

        public List<List<Integer>> levelOrderBottom(TreeNode root) {
            // Use LinkedList to allow efficient addFirst()
            LinkedList<List<Integer>> results = new LinkedList<>();

            if (root == null) return results;

            Queue<TreeNode> queue = new LinkedList<>();
            queue.offer(root);

            while (!queue.isEmpty()) {
                int size = queue.size();
                List<Integer> currLevel = new ArrayList<>();

                for (int i = 0; i < size; i++) {
                    TreeNode node = queue.poll();
                    currLevel.add(node.val);

                    if (node.left != null) queue.offer(node.left);
                    if (node.right != null) queue.offer(node.right);
                }
                // Add to the front of the list so the last level processed
                // ends up at index 0.
                results.addFirst(currLevel);
            }
            return results;
        }
    }

}