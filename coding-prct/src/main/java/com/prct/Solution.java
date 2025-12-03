package com.prct;

import java.util.Scanner;
import java.util.Set;

public class Solution {
    public static void main(String args[] ) throws Exception {
        Scanner stdin = new Scanner(System.in);

        Set<String> fraudDescriptors = Set.of("ONLINE STORE", "ECOMMERCE", "RETAIL","SHOP", "GENERAL MERCHANDISE");

        int count = 0;
        while (stdin.hasNext()) {
            String line = stdin.nextLine();
            count += 1;
            if (count == 1)
                continue;

            String[] parts = line.split(",");
            String businessName = parts[3];

            if (businessName == null || businessName.isEmpty())
                continue;

            String status = "VERIFIED";

            String fullStatementDescriptor = parts[1];
            String shortStatementDescriptor = parts[2];

            if ((fullStatementDescriptor.length() < 5 || fullStatementDescriptor.length() > 31) || (shortStatementDescriptor.length() < 5 || shortStatementDescriptor.length() > 31))
                status = "NOT VERIFIED";

            if (fraudDescriptors.contains(fullStatementDescriptor) || fraudDescriptors.contains(shortStatementDescriptor))
                status = "NOT VERIFIED";

            StringBuilder results = new StringBuilder();
            if (status.equals("VERIFIED")) {
                results.append(status)
                        .append(":")
                        .append("      ")
                        .append(businessName);

            } else {
                results.append(status)
                        .append(":")
                        .append("  ")
                        .append(businessName);

            }
            System.out.println(results);
        }

    }

}