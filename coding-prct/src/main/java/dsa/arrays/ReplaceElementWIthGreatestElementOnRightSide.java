package dsa.arrays;

public class ReplaceElementWIthGreatestElementOnRightSide {

    /*
    **The Trick in One Line:**
    Instead of recomputing the maximum for each element, work **backwards** and use the **already-computed maximum** from the right.

    **How it works:**
        1. Start from the end with `max = -1`
        2. For each element (moving left):
           - Replace it with the current `max`
           - Update `max` using the **original value** of the next element
        3. This works because:
           `new[i] = max(arr[i+1], new[i+1])`

    **Why it's efficient:**
        - **O(n) time** (single pass)
        - **O(1) extra space** (if done in-place)
        - Avoids O(n²) repeated scanning

    **Key Insight:**
        The maximum for position `i` is just the maximum between:
        1. The element immediately to its right (`arr[i+1]`)
        2. The maximum of everything further right (`new[i+1]`)

     This is essentially **dynamic programming** where you reuse previously computed results!
     */

    static class Solution {

        // new[0] = max(arr[1:5])
        // new[1] = max(arr[2:5])
        // new[0] = max(arr[1], new[1]) -- trick

        public int[] replaceElements(int[] arr) {
            int rightMax = -1;

            for (int i = arr.length-1; i >= 0; i--) {
                int currRightMax = Math.max(arr[i], rightMax);
                arr[i] = rightMax;
                rightMax = Math.max(currRightMax, rightMax);
            }
            return arr;
        }
    }

}
