package dsa.arrays;

/**
 * Time Complexity: O(n + m) where n and m are lengths of strings Word1 and Word2.
 * Space Complexity: O(n + m) for the output string.
 *
 * Pattern: Two Pointers
 *
 * Trick:
 * 1. Simultanously iterate through both strings using independent pointers.
 * 2. Alternately append characters from each string to the result.
 * 3. Handle remaining characters from the longer string by appending them after the loop.
 */
public class MergeAlternatively {

    static class Solution {

        public String mergeAlternately(String word1, String word2) {
            char[] word1Chars = word1.toCharArray();
            char[] word2Chars = word2.toCharArray();

            int n1 = word1Chars.length;
            int n2 = word2Chars.length;

            char[] result = new char[n1+n2];

            int i = 0;
            int j = 0;

            boolean isWord1 = true;
            int curr = 0;

            while (i < n1 && j < n2) {
                if (isWord1) {
                    result[curr++] = word1Chars[i++];
                    isWord1 = false;
                } else {
                    result[curr++] = word2Chars[j++];
                    isWord1 = true;
                }
            }

            while (i < n1) {
                result[curr++] = word1Chars[i++];
            }

            while (j < n2) {
                result[curr++] = word2Chars[j++];
            }

            return new String(result);
        }
    }

}
