package company.greyorange.arrays;

import java.util.*;

//Sample Input/Output
//Input: strs = ["eat","tea","tan","ate","nat","bat"]
//Output: [["bat"],["nat","tan"],["ate","eat","tea"]]

/**
 * Time Complexity: O(N * K log K) where N is number of strings and K is max string length.
 * Space Complexity: O(N * K) for storing the map of strings.
 *
 * Pattern: Sorting & Hashing
 *
 * Trick:
 * 1. Convert each string to a sorted char array to create a canonical "anagram key".
 * 2. Use a Map to group original strings by this key.
 * 3. Return the grouped values as a list of lists.
 */
public class ArrangeAnagrams {

    public static void main(String[] args) {
        String[] strs = new String[] {"eat","tea","tan","ate","nat","bat"};

        List<List<String>> resultList = arrangeAnagrams(strs);
        System.out.println(resultList);
    }

    private static List<List<String>> arrangeAnagrams(String[] strs) {
        Map<String, List<String>> anagramMap = new HashMap<>();
        for (String str : strs) {
            char[] charArray = str.toCharArray();
            Arrays.sort(charArray);
            List<String> list = anagramMap.getOrDefault(Arrays.toString(charArray), new ArrayList<>());
            list.add(str);
            anagramMap.put(Arrays.toString(charArray), list);
        }
        return new ArrayList<>(anagramMap.values());
    }

}
