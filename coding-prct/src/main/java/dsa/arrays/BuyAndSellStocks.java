package dsa.arrays;

/**
 * Time Complexity: O(n)
 * Space Complexity: O(1)
 *
 * Pattern: Greedy / Dynamic Programming (Single Pass)
 *
 * Trick:
 * 1. Track the minimum price encountered so far.
 * 2. At each step, calculate the potential profit if sold at current price.
 * 3. Update 'maxProfit' and 'minPrice' accordingly in a single pass.
 */
public class BuyAndSellStocks {

    static class Solution {
        public int maxProfit(int[] prices) {
            int maxProfit = 0;
            int minPrice = prices[0];

            for (int currentPrice : prices) {
                maxProfit = Math.max(maxProfit, currentPrice - minPrice);
                if (currentPrice < minPrice)
                    minPrice = currentPrice;
            }
            return maxProfit;

        }
    }

}