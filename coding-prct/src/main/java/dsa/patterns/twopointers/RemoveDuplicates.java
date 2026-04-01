package dsa.patterns.twopointers;

import java.util.List;

// Trick: Use 1 slow pointer and 1 fast pointer
public class RemoveDuplicates {

    static class Solution {

        // [0,0,1,1,1,2,2,3,3,4]
        //      i,j
        public static int removeDuplicates(List<Integer> arr) {
            int slow = 0;
            for (int fast = 0; fast < arr.size(); fast++) {
                if (!arr.get(fast).equals(arr.get(slow))) {
                    slow++;
                    arr.set(slow, arr.get(fast));
                }
            }
            return slow + 1;
        }
    }

}
