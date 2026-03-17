package dsa.binarysearch;

/**
 * Time Complexity: O(log n)
 * Space Complexity: O(1)
 *
 * Pattern: Binary Search for Insertion Position.
 *
 * Tricks:
 * 1. Midpoint Calculation: Uses 'l + (r - l) / 2' to prevent integer overflow.
 * 2. Post-Loop Correction: If the target isn't found, 'mid' points to the last checked element. 
 *    If nums[mid] is smaller than target, the insertion point is 'mid + 1'; otherwise, it's 'mid'.
 */
public class SearchInsertPosition {

    public static void main(String[] args) {
        int[] nums = {1, 3, 5, 6};
        int target = 7;

        System.out.println(getTargetPosition(nums, target));
    }

    private static int getTargetPosition(int[] nums, int target) {
        int n = nums.length;

        int l = 0;
        int r = n - 1;

        int mid = l + (r - l) / 2;

        while (l <= r) {
            mid = l + (r - l) / 2;

            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }

        if (nums[mid] < target) {
            return mid + 1;
        } else {
            return mid;
        }
    }
}
