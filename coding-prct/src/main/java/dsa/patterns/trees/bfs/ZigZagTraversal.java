package dsa.patterns.trees.bfs;

import dsa.patterns.trees.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


// Trick: Use the LinkedList properties to add elements to either the front or the back depending on the direction.
public class ZigZagTraversal {

    static class Solution {

        public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
            List<List<Integer>> results = new ArrayList<>();
            if (root == null) return results;

            Queue<TreeNode> queue = new LinkedList<>();
            queue.add(root);
            boolean leftToRight = true;

            while (!queue.isEmpty()) {
                int size = queue.size();
                // Use LinkedList to take advantage of addFirst/addLast
                LinkedList<Integer> currLevel = new LinkedList<>();

                for (int i = 0; i < size; i++) {
                    TreeNode node = queue.poll();

                    if (leftToRight) {
                        currLevel.addLast(node.val);
                    } else {
                        currLevel.addFirst(node.val);
                    }

                    if (node.left != null) queue.add(node.left);
                    if (node.right != null) queue.add(node.right);
                }

                results.add(currLevel);
                leftToRight = !leftToRight; // Flip the direction
            }
            return results;
        }
    }

}