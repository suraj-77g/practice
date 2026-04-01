package dsa.patterns.twopointers;

// Trick: Use 1 slow pointer and 1 fast pointer
public class RemoveDuplicates {

    static class Solution {

        // [0,0,1,1,1,2,2,3,3,4]
        //      i,j
        public int removeDuplicates(int[] nums) {
            int n = nums.length;
            if (n <= 1)
                return n;

            int i = 0;
            int j = 0;

            while (j < n) {
                while (j < n && nums[j] == nums[i]) {
                    j++;
                }
                if (j < n) {
                    i++;
                    nums[i] = nums[j];
                    j++;
                }
            }

            return i+1;
        }
    }

}
