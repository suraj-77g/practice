package com.dsa;

/**
 * Recursion Examples (Factorial and Fibonacci)
 * 
 * Time Complexity:
 * - Factorial: O(n)
 * - Fibonacci: O(2^n) - naive recursive.
 * 
 * Space Complexity:
 * - Factorial: O(n) recursion stack.
 * - Fibonacci: O(n) recursion stack.
 */
public class RecursionDemo {

    public static long factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }

    public static int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.println("--- Recursion Demo ---");
        
        System.out.println("Factorial of " + n + ": " + factorial(n));
        
        System.out.print("Fibonacci series up to " + n + ": ");
        for (int i = 0; i <= n; i++) {
            System.out.print(fibonacci(i) + " ");
        }
        System.out.println();
    }
}
