package com.arrays.two_pointers;

public class MiddleOfLL {

    static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        int length = 77;

        Node head = null;
        Node tail = null;

        for (int i = 0; i < length; i++) {
            Node newNode = new Node(i);
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.next = newNode;
                tail = newNode;
            }
        }

        Node middleNode = findMiddleNode(head);
        System.out.println("Middle = " + middleNode.value);
    }

    private static Node findMiddleNode(Node head) {
        if (head == null) return null;

        Node slow = head;
        Node fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow; // returns 2nd middle in even case
    }
}