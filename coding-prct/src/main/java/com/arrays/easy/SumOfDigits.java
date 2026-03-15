package com.arrays.easy;

public class SumOfDigits {

    public static void main(String[] args) {
        int n = 100236;

        int sum = sumOfDigits(n);
        System.out.println(sum);
    }

    /*
        Time Complexity: O(d) where d = number of digits
        1002/10 = 100 , R => 2
        100/10 = 10, R => 0
        10/10 = 1, R => 0
        1/10 = 0, R=> 1
    */
    private static int sumOfDigits(int n) {
        int sum = 0;

        while (n > 0) {
            sum += n % 10;
            n = n / 10;
        }
        return sum;
    }

}