package com.dsa;

/**
 * Graph Implementation (Adjacency List)
 * 
 * Time Complexity:
 * - Add Edge: O(1)
 * - BFS: O(V + E)
 * - DFS: O(V + E)
 * 
 * Space Complexity: O(V + E) to store adjacency lists.
 */
public class Graph {
    private int V;
    private Node[] adj;

    private static class Node {
        int data;
        Node next;

        Node(int data) {
            this.data = data;
        }
    }

    public Graph(int V) {
        this.V = V;
        this.adj = new Node[V];
    }

    public void addEdge(int src, int dest) {
        // Undirected graph
        Node newNode = new Node(dest);
        newNode.next = adj[src];
        adj[src] = newNode;

        newNode = new Node(src);
        newNode.next = adj[dest];
        adj[dest] = newNode;
    }

    public void bfs(int start) {
        boolean[] visited = new boolean[V];
        int[] queue = new int[V];
        int front = 0, rear = 0;

        visited[start] = true;
        queue[rear++] = start;

        System.out.print("BFS (start " + start + "): ");
        while (front < rear) {
            int current = queue[front++];
            System.out.print(current + " ");

            Node temp = adj[current];
            while (temp != null) {
                if (!visited[temp.data]) {
                    visited[temp.data] = true;
                    queue[rear++] = temp.data;
                }
                temp = temp.next;
            }
        }
        System.out.println();
    }

    public void dfs(int start) {
        boolean[] visited = new boolean[V];
        System.out.print("DFS (start " + start + "): ");
        dfsRec(start, visited);
        System.out.println();
    }

    private void dfsRec(int current, boolean[] visited) {
        visited[current] = true;
        System.out.print(current + " ");

        Node temp = adj[current];
        while (temp != null) {
            if (!visited[temp.data]) {
                dfsRec(temp.data, visited);
            }
            temp = temp.next;
        }
    }

    public static void main(String[] args) {
        Graph g = new Graph(4);
        System.out.println("--- Graph Demo ---");

        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 2);
        g.addEdge(2, 3);

        g.bfs(2);
        g.dfs(2);
    }
}
