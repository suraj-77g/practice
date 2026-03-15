package com.company.greyorange.arrays;

/*
    Sample Input/Output
    Input: nums = [100, 4, 200, 1, 3, 2]
    Output: 4 (Sequence is [1, 2, 3, 4])
*/

import java.util.HashSet;
import java.util.Set;

public class LongestConsecutiveSequence {

    public static void main(String[] args) {
        int[] arr = {100, 4, 200, 1, 3, 2, 5};
        int longest = getLongestConsecutiveSeq(arr);
        System.out.println(longest);
    }

    private static int getLongestConsecutiveSeq(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int num : arr) {
            set.add(num);
        }

        int longest = 0;

        for (int num : arr) {
            int currentStreak = 1;
            if (!set.contains(num-1)) {
                int currentNum = num;
                while (set.contains(currentNum+1)) {
                    currentNum++;
                    currentStreak++;
                }
            }
            longest = Math.max(longest, currentStreak);
        }
        return longest;
    }

}