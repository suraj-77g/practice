package dsa.arrays.twopointers;

import java.util.ArrayList;
import java.util.List;

/**
 * Time Complexity: O(n)
 * Space Complexity: O(1) (excluding output list)
 *
 * Pattern: Two Pointers / Interval Merging
 *
 * Trick:
 * 1. Use an outer loop to start a range and an inner loop to extend it.
 * 2. Extend the range as long as nums[end+1] == nums[end] + 1.
 * 3. Format the string based on whether start == end (single number) or not (range).
 */
public class SummaryRanges {

    static class Solution {

        public List<String> summaryRanges(int[] nums) {
            int n = nums.length;

            int end = 0;

            List<String> ranges = new ArrayList<>();

            while (end < n) {
                int start = end;
                while (end <= n-2 && (nums[end + 1] - nums[end]) == 1) {
                    end++;
                }

                String str = null;
                if (start == end) {
                    str = "" + nums[end];
                } else {
                    str = nums[start] + "->" + nums[end];
                }

                ranges.add(str);
                end++;
            }
            return ranges;
        }
    }

}
