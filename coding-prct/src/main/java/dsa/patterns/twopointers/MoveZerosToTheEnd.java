package dsa.patterns.twopointers;

// Trick: Slow+Fast pointers, swapping logic
public class MoveZerosToTheEnd {

    static class Solution {

        public void moveZeroes(int[] nums) {
            // [0,1,0,3,12]

            int i = 0;
            int n = nums.length;

            for (int j = 0; j < n; j++) {
                if (nums[j] != 0) {
                    int tmp = nums[i];
                    nums[i] = nums[j];
                    nums[j] = tmp;
                    i++;
                }
            }
        }

    }

}