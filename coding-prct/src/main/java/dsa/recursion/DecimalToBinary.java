package dsa.recursion;

public class DecimalToBinary {

    public static void main(String[] args) {
        System.out.println(decimalToBinary(10, ""));
    }

    public static String decimalToBinary(int num, String result) {
        if (num == 0) {
            return result;
        }

        result = result + num % 2;
        return decimalToBinary(num / 2, result);
    }

}
