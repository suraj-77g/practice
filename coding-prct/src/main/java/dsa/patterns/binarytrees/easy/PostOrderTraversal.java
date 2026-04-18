package dsa.patterns.binarytrees.easy;

import dsa.patterns.binarytrees.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class PostOrderTraversal {

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
        public List<Integer> postorderTraversal(TreeNode root) {
            List<Integer> result = new ArrayList<>();
            traversePostOrder(root, result);
            return result;
        }

        private void traversePostOrder(TreeNode root, List<Integer> result) {
            if (root == null)
                return;

            traversePostOrder(root.left, result);
            traversePostOrder(root.right, result);
            result.add(root.val);
        }
    }

}