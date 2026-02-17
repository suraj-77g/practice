package com.arrays;

import java.util.HashMap;
import java.util.Map;

public class TwoSum {

    public static void main(String[] args) {
        int[] arr = new int[] {2,1,4,6,0,9};
        int target = 10;

        int[] result = twoSum(target, arr);
        if (result.length > 0)
            System.out.print(result[0] + ", " + result[1]);
    }

    private static int[] twoSum(int target, int[] arr) {
        Map<Integer, Integer> complementMap = new HashMap<>();

        for (int i = 0; i < arr.length; i++) {
            int complement = target - arr[i];
            if (complementMap.containsKey(complement)) {
                return new int[] {complementMap.get(complement), i};
            }
            complementMap.put(arr[i], i);
        }

        return new int[] {};
    }

}