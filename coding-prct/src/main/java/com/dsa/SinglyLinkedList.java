package com.dsa;

/**
 * Singly Linked List Implementation
 * 
 * Time Complexity:
 * - Access: O(n)
 * - Search: O(n)
 * - Insertion: O(1) at head, O(n) at tail
 * - Deletion: O(1) at head, O(n) at tail
 * 
 * Space Complexity: O(n) to store n elements.
 */
public class SinglyLinkedList {
    private Node head;
    private int size;

    private static class Node {
        int data;
        Node next;

        Node(int data) {
            this.data = data;
            this.next = null;
        }
    }

    public SinglyLinkedList() {
        this.head = null;
        this.size = 0;
    }

    // Add to the end of the list
    public void add(int data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    // Delete first occurrence of data
    public boolean delete(int data) {
        if (head == null) return false;

        if (head.data == data) {
            head = head.next;
            size--;
            return true;
        }

        Node current = head;
        while (current.next != null && current.next.data != data) {
            current = current.next;
        }

        if (current.next != null) {
            current.next = current.next.next;
            size--;
            return true;
        }

        return false;
    }

    // Search for data
    public boolean search(int data) {
        Node current = head;
        while (current != null) {
            if (current.data == data) return true;
            current = current.next;
        }
        return false;
    }

    // Reverse the list in-place
    public void reverse() {
        Node prev = null;
        Node current = head;
        Node next = null;
        while (current != null) {
            next = current.next;
            current.next = prev;
            prev = current;
            current = next;
        }
        head = prev;
    }

    public void display() {
        Node current = head;
        while (current != null) {
            System.out.print(current.data + " -> ");
            current = current.next;
        }
        System.out.println("null");
    }

    public int getSize() {
        return size;
    }

    public static void main(String[] args) {
        SinglyLinkedList list = new SinglyLinkedList();
        System.out.println("--- Singly Linked List Demo ---");
        list.add(10);
        list.add(20);
        list.add(30);
        list.add(40);
        
        System.out.print("Initial list: ");
        list.display();

        System.out.println("Search 20: " + list.search(20));
        System.out.println("Search 50: " + list.search(50));

        System.out.println("Delete 20: " + list.delete(20));
        System.out.print("After delete 20: ");
        list.display();

        System.out.println("Reverse list:");
        list.reverse();
        list.display();

        list.delete(40);
        list.delete(30);
        list.delete(10);
        System.out.print("After deleting all: ");
        list.display();
    }
}
