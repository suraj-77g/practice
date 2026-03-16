package com.dsa.binarysearch;

/**
 * Time Complexity: O(log x)
 * Space Complexity: O(1)
 *
 * Pattern: Binary Search on Range.
 *
 * Tricks:
 * 1. Integer Overflow: Cast 'mid' to 'long' before squaring to prevent overflow when x is large.
 * 2. Range Reduction: For x >= 4, sqrt(x) is always less than or equal to x/2.
 * 3. Floor Value: When searching for 'floor(sqrt(x))', after the loop (while l <= r), 
 *    'right' is the largest integer such that right * right <= x.
 */
public class Sqrt {

    class Solution {
        public int mySqrt(int x) {
            int left = 1;
            int right = x;

            // Optimize upper bound: sqrt(x) <= x/2 for x >= 4
            if (x < 2) return x;
            right = x / 2;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                long square = (long) mid * mid;  // Prevent overflow

                if (square == x) {
                    return mid;
                } else if (square > x) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }

            return right;  // right is the largest int where right*right <= x
        }
    }

}
