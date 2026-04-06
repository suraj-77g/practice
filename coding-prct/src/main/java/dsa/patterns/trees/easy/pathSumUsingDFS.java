package dsa.patterns.trees.easy;

import dsa.patterns.trees.TreeNode;

public class pathSumUsingDFS {

    static  class Solution {

        public Boolean pathSum(TreeNode root, Integer target) {
            int sum = 0;
            return dfs(root, sum, target);
        }

        boolean dfs(TreeNode root, int sum, Integer target) {
            if (root == null) {
                return false;
            }

            sum += root.val;
            if (root.left == null && root.right == null && target == sum)
                return true;

            return dfs(root.left, sum, target) || dfs(root.right, sum, target);
        }
    }

}