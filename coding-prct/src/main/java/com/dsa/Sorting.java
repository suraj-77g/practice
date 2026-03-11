package com.dsa;

/**
 * Sorting Implementations (MergeSort and QuickSort)
 * 
 * Time Complexity:
 * - MergeSort: O(n log n) average/worst case.
 * - QuickSort: O(n log n) average, O(n^2) worst case.
 * 
 * Space Complexity:
 * - MergeSort: O(n) auxiliary space.
 * - QuickSort: O(log n) recursion stack.
 */
public class Sorting {

    // --- MergeSort ---
    public static void mergeSort(int[] arr, int l, int r) {
        if (l < r) {
            int m = l + (r - l) / 2;
            mergeSort(arr, l, m);
            mergeSort(arr, m + 1, r);
            merge(arr, l, m, r);
        }
    }

    private static void merge(int[] arr, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;

        int[] L = new int[n1];
        int[] R = new int[n2];

        for (int i = 0; i < n1; ++i) L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j) R[j] = arr[m + 1 + j];

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }

        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    // --- QuickSort ---
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }

    public static void printArray(int[] arr) {
        for (int i : arr) System.out.print(i + " ");
        System.out.println();
    }

    public static void main(String[] args) {
        int[] arr1 = {12, 11, 13, 5, 6, 7};
        int[] arr2 = {10, 7, 8, 9, 1, 5};

        System.out.println("--- Sorting Demo ---");
        
        System.out.print("Initial Array 1: ");
        printArray(arr1);
        mergeSort(arr1, 0, arr1.length - 1);
        System.out.print("MergeSort Result: ");
        printArray(arr1);

        System.out.print("\nInitial Array 2: ");
        printArray(arr2);
        quickSort(arr2, 0, arr2.length - 1);
        System.out.print("QuickSort Result: ");
        printArray(arr2);
    }
}
