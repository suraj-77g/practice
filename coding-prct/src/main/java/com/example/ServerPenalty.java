package com.example;

public class ServerPenalty {

    public static void main(String[] args) {
        String requests = "YNNYYNY";
        int k = calculateMinPenaltyHour(requests);
        System.out.println("Best hour: " + k);
    }

    private static int calculateMinPenaltyHour(String requests) {
        int n = requests.length();

        int countY = 0;
        for (int i = 0; i < n; i++) {
            if (requests.charAt(i) == 'Y') {
                countY++;
            }
        }

        int minPenalty = countY;
        int currentPenalty = countY;
        int bestHour = 0;

        for (int i = 0; i < n; i++) {
            char req = requests.charAt(i);

            if (req == 'Y') {
                currentPenalty--;
            } else {
                currentPenalty++;
            }

            if (currentPenalty < minPenalty) {
                minPenalty = currentPenalty;
                bestHour = i + 1;
            }
        }
        System.out.println("Min penalty: " + minPenalty);
        return bestHour;
    }

}