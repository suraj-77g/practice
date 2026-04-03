package dsa.patterns.twopointers;

import java.util.List;

/*

Sliding window fixed size Template:

    private static W slidingWindowFixed(List<T> input, int windowSize) {
        W ans = window = input.subList(0, windowSize);
        for (int right = windowSize; right < input.length(); ++right) {
            int left = right - windowSize
            remove input[left] from window
            append input[right] to window
            ans = optimal(ans, window);
        }
        return ans
    }

 */
public class SubArraySumFixed {

    static class Solution {

        public int subarraySumFixed(List<Integer> nums, int k) {
            int n = nums.size();

            int currSum = 0;
            for (int i = 0; i < k; i++) {
                currSum += nums.get(i);
            }

            int maxSum = currSum;

            int i = 0;
            for (int j = k; j < n; j++) {
                currSum += nums.get(j);
                currSum -= nums.get(i);
                i++;
                maxSum = Math.max(maxSum, currSum);
            }

            return maxSum;
        }
    }

}