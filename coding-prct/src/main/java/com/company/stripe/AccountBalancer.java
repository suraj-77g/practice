package com.company.stripe;

import java.util.HashMap;
import java.util.Map;

public class AccountBalancer
{
    public static void main( String[] args )
    {
        String[][] transactions = {
                {"Alice", "Bob", "10"},
                {"Bob", "Charlie", "5"},
                {"Alice", "Charlie", "3"},
                {"David", "Eve", "2"}
        };

        System.out.println("Final Balances:");
        Map<String, Integer> balances = calculateBalances(transactions);
        for (Map.Entry<String, Integer> entry : balances.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static Map<String, Integer> calculateBalances(String[][] transactions) {
        Map<String, Integer> balanceMap = new HashMap<>();

        for (String[] transaction : transactions) {
            String sender = transaction[0];
            String receiver = transaction[1];
            int amount = Integer.parseInt(transaction[2]);

            balanceMap.put(sender, balanceMap.getOrDefault(sender, 0) - amount);
            balanceMap.put(receiver, balanceMap.getOrDefault(receiver, 0) + amount);
        }
        return balanceMap;
    }

}