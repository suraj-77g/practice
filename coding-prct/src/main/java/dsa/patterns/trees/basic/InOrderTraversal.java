package dsa.patterns.trees.basic;

import dsa.patterns.trees.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class InOrderTraversal {

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

        public List<Integer> inorderTraversal(TreeNode root) {
            List<Integer> inOrder = new ArrayList<>();
            traverseInOrder(root, inOrder);
            return inOrder;
        }

        private void traverseInOrder(TreeNode root, List<Integer> result) {
            if (root == null) return;

            traverseInOrder(root.left, result);
            result.add(root.val);
            traverseInOrder(root.right, result);
        }
    }

}