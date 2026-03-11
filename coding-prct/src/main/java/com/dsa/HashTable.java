package com.dsa;

/**
 * HashTable Implementation (Using Chaining with Singly Linked List)
 * 
 * Time Complexity:
 * - Insertion: O(1) average, O(n) worst case
 * - Search: O(1) average, O(n) worst case
 * - Deletion: O(1) average, O(n) worst case
 * (Where n is the number of elements in a bucket)
 * 
 * Space Complexity: O(m + n) where m is number of buckets and n is number of elements.
 */
public class HashTable {
    private static class Node {
        int key;
        int value;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node[] buckets;
    private int capacity;
    private int size;

    public HashTable(int capacity) {
        this.capacity = capacity;
        this.buckets = new Node[capacity];
        this.size = 0;
    }

    private int getIndex(int key) {
        return Math.abs(key % capacity);
    }

    public void put(int key, int value) {
        int index = getIndex(key);
        Node head = buckets[index];

        // Update if key already exists
        Node current = head;
        while (current != null) {
            if (current.key == key) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        // Insert at head of the bucket list
        Node newNode = new Node(key, value);
        newNode.next = buckets[index];
        buckets[index] = newNode;
        size++;
    }

    public Integer get(int key) {
        int index = getIndex(key);
        Node current = buckets[index];
        while (current != null) {
            if (current.key == key) return current.value;
            current = current.next;
        }
        return null;
    }

    public boolean remove(int key) {
        int index = getIndex(key);
        Node current = buckets[index];
        Node prev = null;

        while (current != null) {
            if (current.key == key) {
                if (prev == null) {
                    buckets[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public static void main(String[] args) {
        HashTable table = new HashTable(10);
        System.out.println("--- Hash Table Demo ---");
        
        table.put(1, 100);
        table.put(11, 1100); // Collision with 1
        table.put(2, 200);

        System.out.println("Get 1: " + table.get(1));
        System.out.println("Get 11: " + table.get(11));
        System.out.println("Get 2: " + table.get(2));
        System.out.println("Get 3 (not exist): " + table.get(3));

        System.out.println("Remove 11: " + table.remove(11));
        System.out.println("Get 11 after removal: " + table.get(11));
        System.out.println("Size: " + table.size());
    }
}
