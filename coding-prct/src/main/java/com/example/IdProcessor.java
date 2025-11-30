package com.example;

import java.util.*;

public class IdProcessor {

    public static void main(String[] args) {
        IdProcessor processor = new IdProcessor();

        // Define the sample data
        String logData = "Error processing txn_1. Details: cus_A990 was involved. Refer to txn_1001 for reconciliation.";
        Set<String> masterIds = new HashSet<>(Arrays.asList("txn_1001", "cus_A99", "txn_555"));
        System.out.println("1. Master IDs: " + masterIds);

        // 1. Run Part 1
        List<String> extracted = processor.extractIds(logData);
        System.out.println("1. Extracted IDs: " + extracted);

        // 2. Run Part 2
        List<String> exactMatches = processor.findExactMatches(extracted, masterIds);
        System.out.println("2. Exact Matches: " + exactMatches);

        // 3. Run Part 3
        List<String> prefixMatches = processor.findPrefixMatches(extracted, masterIds);
        System.out.println("3. Prefix Matches: " + prefixMatches);
    }

    private List<String> findExactMatches(List<String> extracted, Set<String> masterIds) {
        List<String> exactMatches = new ArrayList<>();
        for (String extractedId: extracted) {
            if (masterIds.contains(extractedId)) {
                exactMatches.add(extractedId);
            }
        }
        return exactMatches;
    }

    private List<String> findPrefixMatches(List<String> extracted, Set<String> masterIds) {
        List<String> prefixMatches = new ArrayList<>();
        for (String extractedId: extracted) {
            for (String masterId: masterIds) {
                if (masterId.startsWith(extractedId)) {
                    prefixMatches.add(masterId);
                }
            }
        }
        return prefixMatches;
    }

    private List<String> extractIds(String logData) {
        return Arrays.asList(logData.split("\\W+"));
    }

}