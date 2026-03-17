package dsa.arrays;

import java.util.HashMap;
import java.util.Map;

/**
 * Time Complexity: O(n) where n is the length of string 's'.
 * Space Complexity: O(1) for the fixed-size Roman numeral map.
 *
 * Pattern: Peek Ahead Comparison
 *
 * Trick:
 * 1. For each character, check the value of the next character.
 * 2. If current value < next value, subtract current from next (subtraction rule) and skip the next character.
 * 3. Otherwise, just add current value.
 */
public class RomanToInteger {

    static class Solution {
        public int romanToInt(String s) {
            Map<Character, Integer> romanMap = new HashMap<>();
            romanMap.put('I', 1);
            romanMap.put('V', 5);
            romanMap.put('X', 10);
            romanMap.put('L', 50);
            romanMap.put('C', 100);
            romanMap.put('D', 500);
            romanMap.put('M', 1000);

            int sum = 0;

            for (int i = 0; i < s.length(); i++) {
                Integer val = romanMap.get(s.charAt(i));

                if (i == s.length() - 1) {
                    sum = sum + val;
                    break;
                }

                Integer nextVal = romanMap.get(s.charAt(i+1));
                if (val < nextVal) {
                    i++;
                    sum = sum + (nextVal - val);
                } else {
                    sum = sum + val;
                }
            }
            return sum;
        }
    }

}
