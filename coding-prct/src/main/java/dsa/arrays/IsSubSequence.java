package dsa.arrays;

/**
 * Time Complexity: O(t) where t is the length of the target string 't'.
 * Space Complexity: O(1) for pointers.
 *
 * Pattern: Two Pointers
 *
 * Trick:
 * 1. Maintain a pointer for the potential subsequence 's'.
 * 2. Iterate through 't' and increment 'sIndex' only when a match is found.
 * 3. After the loop, if 'sIndex' matches length of 's', then 's' is a subsequence of 't'.
 */
public class IsSubSequence {

    static class Solution {
        public boolean isSubsequence(String s, String t) {
            int sIndex = 0;
            int tIndex = 0;

            while (tIndex < t.length() && sIndex < s.length()) {
                if (s.charAt(sIndex) == t.charAt(tIndex)) {
                    sIndex++;
                }
                tIndex++;
            }

            return sIndex == s.length();
        }
    }

}