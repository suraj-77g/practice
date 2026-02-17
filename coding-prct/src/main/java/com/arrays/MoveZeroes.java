package com.arrays;

public class MoveZeroes {

    public static void main(String[] args) {
        int[] arr = new int[] {0, 1, 0, 3, 12};
        moveZeroes(arr);

        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
        }
    }

    private static void moveZeroes(int[] arr) {
        int s = 0;
        for (int f = 0; f < arr.length; f++)
            if (arr[f] != 0) {
                int t = arr[s];
                arr[s++] = arr[f];
                arr[f] = t;
            }
    }

}