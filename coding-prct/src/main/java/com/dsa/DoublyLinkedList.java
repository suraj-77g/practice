package com.dsa;

/**
 * Doubly Linked List Implementation
 * 
 * Time Complexity:
 * - Access: O(n)
 * - Search: O(n)
 * - Insertion: O(1) at head, O(n) at tail
 * - Deletion: O(1) at head, O(n) at tail
 * 
 * Space Complexity: O(n)
 */
public class DoublyLinkedList {
    private Node head;
    private Node tail;
    private int size;

    private static class Node {
        int data;
        Node prev;
        Node next;

        Node(int data) {
            this.data = data;
        }
    }

    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // Add to the end of the list
    public void add(int data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    // Delete first occurrence of data
    public boolean delete(int data) {
        if (head == null) return false;

        Node current = head;
        while (current != null) {
            if (current.data == data) {
                if (current == head) {
                    head = head.next;
                    if (head != null) {
                        head.prev = null;
                    } else {
                        tail = null;
                    }
                } else if (current == tail) {
                    tail = tail.prev;
                    if (tail != null) {
                        tail.next = null;
                    } else {
                        head = null;
                    }
                } else {
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                }
                size--;
                return true;
            }
            current = current.next;
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

    public void displayForward() {
        Node current = head;
        System.out.print("Forward: ");
        while (current != null) {
            System.out.print(current.data + " <-> ");
            current = current.next;
        }
        System.out.println("null");
    }

    public void displayBackward() {
        Node current = tail;
        System.out.print("Backward: ");
        while (current != null) {
            System.out.print(current.data + " <-> ");
            current = current.prev;
        }
        System.out.println("null");
    }

    public static void main(String[] args) {
        DoublyLinkedList list = new DoublyLinkedList();
        System.out.println("--- Doubly Linked List Demo ---");
        
        list.add(10);
        list.add(20);
        list.add(30);

        list.displayForward();
        list.displayBackward();

        System.out.println("Search 20: " + list.search(20));
        System.out.println("Delete 20: " + list.delete(20));
        
        list.displayForward();
        
        System.out.println("Delete 10 (head): " + list.delete(10));
        list.displayForward();
        
        System.out.println("Delete 30 (tail): " + list.delete(30));
        list.displayForward();
    }
}
