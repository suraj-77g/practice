package dsa.patterns.graphs.easy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAdjacencyList {

    public static void main(String[] args) {
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {0, 2}};

        // 1. Initialize the Map
        Map<Integer, List<Integer>> adjMap = new HashMap<>();

        for (int[]  edge : edges) {
            int from = edge[0];
            int to = edge[1];

            adjMap.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
            adjMap.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
        }

        adjMap.forEach((from, to) -> {
            System.out.printf(from + " -> " + to + "\n");
        });

    }

}
