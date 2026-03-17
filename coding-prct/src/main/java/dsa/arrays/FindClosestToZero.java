package dsa.arrays;

/**
 * Time Complexity: O(n)
 * Space Complexity: O(1)
 *
 * Pattern: Absolute Value Comparison
 *
 * Trick:
 * 1. Maintain a 'closest' variable initialized to Integer.MAX_VALUE.
 * 2. Compare absolute values to find proximity to zero.
 * 3. Tie-breaker: If absolute values are equal, pick the larger number (e.g., between -1 and 1, pick 1).
 */
public class FindClosestToZero {
    static class Solution {
        public int findClosestNumber(int[] nums) {
            int closest = Integer.MAX_VALUE;

            for (int num: nums) {
                if (Math.abs(num) == Math.abs(closest)) {
                    closest = Math.max(num, closest);
                } else if (Math.abs(num) < Math.abs(closest)) {
                    closest = num;
                }
            }
            return closest;
        }
    }

}
