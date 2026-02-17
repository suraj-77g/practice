package com.arrays.two_pointers;

import java.util.Arrays;

public class SquaresOfSortedArray {

    public static void main(String[] args) {
        int[] nums = {-4, -1, 0, 3, 10};
        int[] result = sortedSquares(nums);

        System.out.println(Arrays.toString(result));
    }

    public static int[] sortedSquares(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];

        int left = 0;
        int right = n - 1;
        int pos = n - 1;  // fill from end

        while (left <= right) {
            int leftSquare = nums[left] * nums[left];
            int rightSquare = nums[right] * nums[right];

            if (leftSquare > rightSquare) {
                result[pos--] = leftSquare;
                left++;
            } else {
                result[pos--] = rightSquare;
                right--;
            }
        }

        return result;
    }

}
