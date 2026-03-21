package dsa.recursion;

public class SumOfNaturalNumbers {

    public static void main(String[] args) {
        int n = 4;
        System.out.println(sumOfN(n));
    }

    private static int sumOfN(int n) {
        if (n == 0) {
            return 0;
        }
        return sumOfN(n - 1) + n;
    }

}
