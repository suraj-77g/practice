package dsa.recursion;

public class BiggestNumber {

    public static void main(String[] args) {
        int[] arr = {1, 82, 3, 4, 5};

        int result = biggest(arr);
        System.out.println(result);
    }

    private static int biggest(int[] arr) {
        if  (arr == null || arr.length == 0) return 0;
        return biggestElement(0, arr);
    }

    private static int biggestElement(int index, int[] arr) {
        if (index == arr.length) return 0;
        return Math.max(arr[index], biggestElement(index + 1, arr));
    }

}
