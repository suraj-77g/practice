package dsa.patterns.trees;

public class PathSumUsingRecursion {

    static class Solution {

        public boolean pathSum(TreeNode root, int target) {
            if (root == null) {
                return false;
            }

            // if we reach a leaf node, check if the target is equal to the leaf node's value
            if (root.left == null && root.right == null) {
                return target == root.val;
            }

            target -= root.val;

            // check if there's a path from the current node to a leaf that sums to target
            return pathSum(root.left, target) || pathSum(root.right, target);
        }
    }

}
