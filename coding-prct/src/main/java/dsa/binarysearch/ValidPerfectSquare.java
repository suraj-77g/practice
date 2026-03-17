package dsa.binarysearch;

/**
 * Time Complexity: O(log n)
 * Space Complexity: O(1)
 *
 * Pattern: Binary Search on Range.
 * Instead of searching in an array, we search in the range [1, num].
 *
 * Tricks:
 * 1. Monotonic Search Space: Since x*x increases as x increases, we can use binary search to find the root.
 * 2. Integer Overflow: Use 'long' for the product (mid * mid) to prevent overflow during comparison.
 * 3. Range Reduction: For num > 1, the square root is always less than or equal to num/2.
 */
public class ValidPerfectSquare {

    public static void main(String[] args) {
        int num1 = 16;
        int num2 = 14;

        System.out.println("Is " + num1 + " a perfect square? " + isPerfectSquare(num1));
        System.out.println("Is " + num2 + " a perfect square? " + isPerfectSquare(num2));
    }

    public static boolean isPerfectSquare(int num) {
        if (num < 1) return false;
        if (num == 1) return true;

        long left = 1;
        long right = num / 2;

        while (left <= right) {
            long mid = left + (right - left) / 2;
            long square = mid * mid;

            if (square == num) {
                return true;
            } else if (square < num) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return false;
    }
}
