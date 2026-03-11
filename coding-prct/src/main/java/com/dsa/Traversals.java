package com.dsa;

/**
 * Tree Traversals (In-order, Pre-order, Post-order)
 * 
 * Time Complexity: O(n) where n is number of nodes.
 * Space Complexity: O(h) where h is height of tree (recursion stack).
 */
public class Traversals {

    private static class Node {
        int data;
        Node left, right;

        Node(int data) {
            this.data = data;
        }
    }

    public static void preorder(Node node) {
        if (node == null) return;
        System.out.print(node.data + " ");
        preorder(node.left);
        preorder(node.right);
    }

    public static void inorder(Node node) {
        if (node == null) return;
        inorder(node.left);
        System.out.print(node.data + " ");
        inorder(node.right);
    }

    public static void postorder(Node node) {
        if (node == null) return;
        postorder(node.left);
        postorder(node.right);
        System.out.print(node.data + " ");
    }

    public static void main(String[] args) {
        /*
                  1
                /   \
               2     3
              / \
             4   5
        */
        Node root = new Node(1);
        root.left = new Node(2);
        root.right = new Node(3);
        root.left.left = new Node(4);
        root.left.right = new Node(5);

        System.out.println("--- Tree Traversals Demo ---");
        
        System.out.print("Pre-order: ");
        preorder(root);
        System.out.println();

        System.out.print("In-order: ");
        inorder(root);
        System.out.println();

        System.out.print("Post-order: ");
        postorder(root);
        System.out.println();
    }
}
