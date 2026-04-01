package dsa.patterns.twopointers;

import java.util.HashSet;
import java.util.Set;


// Trick: Use sliding window with set
public class LongestSubstringWithoutRepeatingChars {

    static class Solution {

        //        "abcabcbb"
        //          l
        //            r
        public int lengthOfLongestSubstring(String s) {
            int l = 0;
            int n = s.length();

            int maxLength = 0;
            Set<Character> window = new HashSet<>();

            for (int r = 0; r < n; r++) {
                while (window.contains(s.charAt(r))) {
                    window.remove(s.charAt(l++));
                }
                window.add(s.charAt(r));
                maxLength = Math.max(maxLength, r - l + 1);
            }
            return maxLength;
        }
    }

}
