package dsa.patterns.twopointers;

import java.util.List;

// Use two pointers at the ends; area is tracked the max while shrinking the window.
// Always move the pointer at the shorter height to improve area (moving the taller one can’t help).

public class ContainerWithMostWater {

    // Input: [1,8,6,2,5,4,8,3,7]
    // Output: 49

    public static int containerWithMostWater(List<Integer> height) {
        int left = 0;
        int right = height.size() - 1;
        int maxArea = 0;
        while (left < right) {
            int currentArea = Math.min(height.get(left), height.get(right)) * (right - left);
            maxArea = Math.max(maxArea, currentArea);
            if (height.get(left) < height.get(right)) {
                left++;
            } else {
                right--;
            }
        }
        return maxArea;
    }

}
