package com.dsa;

/**
 * Binary Search Implementation (Iterative and Recursive)
 * 
 * Time Complexity: O(log n)
 * Space Complexity: O(1) for iterative, O(log n) recursion stack for recursive.
 */
public class BinarySearch {

    public static int iterativeSearch(int[] arr, int target) {
        int low = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (arr[mid] == target) return mid;
            if (arr[mid] < target) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }

    public static int recursiveSearch(int[] arr, int target, int low, int high) {
        if (low > high) return -1;

        int mid = low + (high - low) / 2;
        if (arr[mid] == target) return mid;
        
        if (arr[mid] < target) {
            return recursiveSearch(arr, target, mid + 1, high);
        } else {
            return recursiveSearch(arr, target, low, mid - 1);
        }
    }

    public static void main(String[] args) {
        int[] sortedArr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
        int target = 23;

        System.out.println("--- Binary Search Demo ---");
        System.out.println("Array: [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]");
        System.out.println("Target: " + target);

        int iterativeResult = iterativeSearch(sortedArr, target);
        System.out.println("Iterative Result: Index " + iterativeResult);

        int recursiveResult = recursiveSearch(sortedArr, target, 0, sortedArr.length - 1);
        System.out.println("Recursive Result: Index " + recursiveResult);

        int notFound = iterativeSearch(sortedArr, 100);
        System.out.println("Search 100: Index " + notFound);
    }
}
