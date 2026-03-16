package com.dsa.binarysearch;

/**
 * Time Complexity: O(log(m * n))
 * Space Complexity: O(1)
 * Pattern: Virtual 1D Array Binary Search
 * Trick: Map 1D index back to 2D coordinates: row = index / cols, col = index % cols.
 */
public class SearchIn2DArray {

    public static boolean searchMatrix(int[][] matrix, int target) {
        int m = matrix.length;
        int n = matrix[0].length;

        int L = 0;
        int R = m*n - 1;

        while (L <= R) {
            int M = L + (R-L)/2;

            int midElement = matrix[M/n][M%n];
            if (midElement == target) {
                return true;
            } else if (midElement > target) {
                R = M - 1;
            } else {
                L = M + 1;
            }
        }
        return false;
    }
}
