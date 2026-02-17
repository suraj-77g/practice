package com.arrays;

public class ReverseArray {

    // Reverse array
    public static void main(String[] args) {
        int[] arr = new int[]{1,2,3,4,5,6,7,8,9};

        reverseArray(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
        }
    }

    private static void reverseArray(int[] arr) {
        int l = 0;
        int r = arr.length-1;

        while (l < r) {
            int tmp = arr[l];
            arr[l++] = arr[r];
            arr[r--] = tmp;
        }
    }

}
