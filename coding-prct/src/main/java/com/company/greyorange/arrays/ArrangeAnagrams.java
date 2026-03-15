package com.company.greyorange.arrays;

import java.util.*;

//Sample Input/Output
//Input: strs = ["eat","tea","tan","ate","nat","bat"]
//Output: [["bat"],["nat","tan"],["ate","eat","tea"]]

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
