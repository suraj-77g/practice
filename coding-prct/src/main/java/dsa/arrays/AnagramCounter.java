package dsa.arrays;

// Maintain a running count of how many characters have matching frequencies between the window and the pattern.
// As the window slides, only update that count by checking if the exiting or entering character breaks or creates a frequency match.

public class AnagramCounter {

    public static void main(String[] args) {
        System.out.println("Anagrams found: " + countAnagrams("cbaebabacd", "abc")); // Output: 2
    }

    private static int countAnagrams(String txt, String pat) {
        int n = txt.length();
        int k = pat.length();
        if (k > n) return 0;

        int[] frqPat = new int[26];
        int[] frqSub = new int[26];
        int matchingChar = 0;
        int ans = 0;

        // 1. Initialize the pattern frequency and the first window
        for (int i = 0; i < k; i++) {
            frqPat[pat.charAt(i) - 'a']++;
            frqSub[txt.charAt(i) - 'a']++;
        }

        // 2. Initial check: how many characters match frequencies at the start?
        for (int i = 0; i < 26; i++) {
            if (frqPat[i] == frqSub[i]) {
                matchingChar++;
            }
        }

        // 3. Check if the first window is an anagram
        if (matchingChar == 26) ans++;

        // 4. Slide the window
        for (int i = k; i < n; i++) {
            int leftCharIdx = txt.charAt(i - k) - 'a';
            int rightCharIdx = txt.charAt(i) - 'a';

            // REMOVE the leftmost character
            if (frqSub[leftCharIdx] == frqPat[leftCharIdx]) matchingChar--;
            frqSub[leftCharIdx]--;
            if (frqSub[leftCharIdx] == frqPat[leftCharIdx]) matchingChar++;

            // ADD the new rightmost character
            if (frqSub[rightCharIdx] == frqPat[rightCharIdx]) matchingChar--;
            frqSub[rightCharIdx]++;
            if (frqSub[rightCharIdx] == frqPat[rightCharIdx]) matchingChar++;

            if (matchingChar == 26) ans++;
        }

        return ans;
    }

}
