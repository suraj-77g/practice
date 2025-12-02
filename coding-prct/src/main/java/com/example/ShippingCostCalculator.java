package com.example;

import java.util.Map;

public class ShippingCostCalculator {

    public static void main(String[] args) {
        Map<String, Integer> targetCosts = Map.of("US", 5,
                "CA", 10,
                "UK", 20);

        Map<String, Integer> productWeights = Map.of("A", 2,
                "B", 12,
                "C", 5);

        Map<String, String> order = Map.of("country", "FR",
                "product", "B");

        System.out.println(getShippingCost(order, targetCosts, productWeights));
    }

    private static int getWeight(Map<String, Integer> productWeights, String product) {
        int weight = productWeights.getOrDefault(product, -1);
        if (weight == -1)
            throw new IllegalArgumentException("Product not found: " + product);
        return weight;
    }

    private static int getTargetCost(Map<String, Integer> targetCosts, String country) {
        if (targetCosts.containsKey(country)) {
            return targetCosts.get(country);
        } else {
            if (!targetCosts.containsKey("US")) {
                throw new IllegalStateException("Config Error: US base cost missing");
            }
            return targetCosts.get("US") * 2;
        }
    }

    private static int getShippingCost(Map<String, String> order, Map<String, Integer> targetCosts, Map<String, Integer> productWeights) {
        int cost = getTargetCost(targetCosts, order.get("country"));
        int weight = getWeight(productWeights, order.get("product"));
        if (weight > 10) {
            cost += 20;
        }
        return cost;
    }

}