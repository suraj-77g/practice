package dsa.arrays.prefixarray;


public class BorderElementsSumK {

    public static void main(String[] args) {
        int[] arr = {4,6,2,0,-1,3,4,9};

        int k = 4;
        int maxSum = getMaxSumWithK(arr, k);
        System.out.println(maxSum);
    }

    private static int getMaxSumWithK(int[] arr, int k) {
        if (arr == null || arr.length == 0) return 0;

        int[] prefixArray = new int[k];
        int[] suffixArray = new int[k];

        // Build prefix array
        prefixArray[0] = arr[0];
        for (int i = 1; i <  k; i++) {
            prefixArray[i] = prefixArray[i-1] + arr[i];
        }

        // Build suffix array
        int right = arr.length - 1;
        suffixArray[k - 1] = arr[right];

        for (int i = k - 2; i >= 0; i--) {
            right--;
            suffixArray[i] = suffixArray[i + 1] + arr[right];
        }

        int maxSum = 0;

        for (int i = 0; i < k; i++) {
            int sum = prefixArray[i];
            if (i < k-1) {
                sum += suffixArray[i+1];
            }
            maxSum = Math.max(maxSum, sum);
        }
        maxSum = Math.max(maxSum, suffixArray[0]);
        return maxSum;
    }

}
