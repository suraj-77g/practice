package dsa.arrays;

/**
 * Time Complexity: O(S) where S is the sum of all characters in all strings.
 * Space Complexity: O(1) (excluding the output string).
 *
 * Pattern: Vertical Scanning
 *
 * Trick:
 * 1. Find the minimum length among all strings to bound the search.
 * 2. Compare characters at each index vertically across all strings.
 * 3. Stop and return the prefix as soon as a mismatch or end of a string is reached.
 */
public class LongestCommonPrefix {

    static class Solution {
        public String longestCommonPrefix(String[] strs) {
            int minLength = Integer.MAX_VALUE;
            for (String s : strs) {
                minLength = Math.min(minLength, s.length());
            }

            StringBuilder result = new StringBuilder();
            int i = 0;
            String a = strs[0];

            while (i < minLength) {
                char curr = a.charAt(i);

                for (int j = 1; j < strs.length; j++) {
                    if (curr != strs[j].charAt(i)) {
                        return result.toString();
                    }
                }

                i++;
                result.append(curr);
            }
            return result.toString();
        }
    }

}