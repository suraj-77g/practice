package dsa.patterns.twopointers;

import java.util.HashSet;
import java.util.Set;

public class LongestSubstringWithoutRepeatingChars {

    static class Solution {

        //        "abcabcbb"
        //          l
        //            r
        public int lengthOfLongestSubstring(String s) {
            int l = 0;
            int r = 0;

            int n = s.length();

            int maxLength = 0;
            Set<Character> window = new HashSet<>();

            while (r < n) {
                if (!window.contains(s.charAt(r))) {
                    window.add(s.charAt(r));
                    r++;
                    maxLength = Math.max(maxLength, window.size());
                    continue;
                } else {
                    while (window.contains(s.charAt(r))) {
                        window.remove(s.charAt(l));
                        l++;
                    }
                    window.add(s.charAt(r));
                    r++;
                }
            }
            return maxLength;
        }
    }

}
