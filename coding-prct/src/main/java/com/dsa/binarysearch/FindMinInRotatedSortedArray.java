package com.dsa.binarysearch;

/**
 * Time Complexity: O(log n)
 * Space Complexity: O(1)
 *
 * Pattern: Binary Search on a Boolean Array (Predicate-based search)
 *
 * Trick:
 * 1. Transform the search space into a virtual Boolean array: [F, F, F, T, T, T].
 *    In this problem, we define a condition (like nums[m] < nums[0]) to split the array into two halves.
 * 2. Find the "First True": The minimum element is the first element that belongs to the "rotated" part.
 * 3. Choice of Reference: Comparing with 'nums[0]' helps identify if we are in the left (larger) sorted part
 *    or the right (smaller) sorted part.
 */
public class FindMinInRotatedSortedArray {

    public int findMin(int[] nums) {
        int n = nums.length;

        int l = 0;
        int r = n - 1;
        int ans = nums[0];

        while (l <= r) {
            int m = l + (r - l)/2;

            if (nums[m] >= nums[0]) {
                l = m + 1;
            } else {
                ans = nums[m];
                r = m - 1;
            }
        }
        return ans;
    }

}
