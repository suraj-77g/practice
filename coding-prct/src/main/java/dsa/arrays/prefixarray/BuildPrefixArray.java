package dsa.arrays.prefixarray;

import java.util.Arrays;

public class BuildPrefixArray {

    public static void main(String[] args) {
        int[] arr = new int[]{5,8,4,-1,7,0,9,10,11,66,4};
        System.out.println(Arrays.toString(buildPrefixArray(arr)));
    }

    private static int[] buildPrefixArray(int[] nums) {
        int n = nums.length;
        if (n == 0) return new int[]{};

        int[] prefixArray = new int[n];
        prefixArray[0] = nums[0];

        for (int i = 1; i < n; i++) {
            prefixArray[i] = prefixArray[i - 1] + nums[i];
        }
        return prefixArray;
    }

}
