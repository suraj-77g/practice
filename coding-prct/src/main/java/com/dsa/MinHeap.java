package com.dsa;

/**
 * MinHeap Implementation (Array Based)
 * 
 * Time Complexity:
 * - Insert: O(log n)
 * - Extract Min: O(log n)
 * - Peek: O(1)
 * - Heapify: O(log n)
 * - Build Heap: O(n) - This is achieved by starting from the last non-leaf node 
 *                  and heapifying down each parent, which is mathematically proven 
 *                   to be linear time.
 * 
 * Space Complexity: O(n)
 */
public class MinHeap {
    private int[] heap;
    private int size;
    private int capacity;

    public MinHeap(int capacity) {
        this.capacity = capacity;
        this.heap = new int[capacity];
        this.size = 0;
    }

    /**
     * Builds a MinHeap from an existing unsorted array in O(n) time.
     */
    public void buildHeap(int[] arr) {
        this.size = arr.length;
        if (size > capacity) {
            this.capacity = size * 2;
            this.heap = new int[capacity];
        }
        System.arraycopy(arr, 0, this.heap, 0, size);

        // Start from the last non-leaf node (index size/2 - 1) 
        // and heapify down each node in reverse order.
        for (int i = (size / 2) - 1; i >= 0; i--) {
            minHeapify(i);
        }
    }

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i) { return 2 * i + 1; }
    private int right(int i) { return 2 * i + 2; }

    public void insert(int val) {
        if (size == capacity) {
            resize();
        }
        size++;
        int i = size - 1;
        heap[i] = val;

        while (i != 0 && heap[parent(i)] > heap[i]) {
            swap(i, parent(i));
            i = parent(i);
        }
    }

    public Integer extractMin() {
        if (size <= 0) return null;
        if (size == 1) {
            size--;
            return heap[0];
        }

        int root = heap[0];
        heap[0] = heap[size - 1];
        size--;
        minHeapify(0);

        return root;
    }

    private void minHeapify(int i) {
        int l = left(i);
        int r = right(i);
        int smallest = i;
        if (l < size && heap[l] < heap[i])
            smallest = l;
        if (r < size && heap[r] < heap[smallest])
            smallest = r;
        if (smallest != i) {
            swap(i, smallest);
            minHeapify(smallest);
        }
    }

    private void swap(int i, int j) {
        int temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void resize() {
        capacity *= 2;
        int[] newHeap = new int[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }

    public void display() {
        for (int i = 0; i < size; i++) {
            System.out.print(heap[i] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("--- Min Heap Build (O(n)) Demo ---");
        int[] unsortedArray = {45, 15, 5, 4, 3, 2};
        MinHeap minHeap = new MinHeap(10);
        
        System.out.print("Unsorted input: ");
        for (int i : unsortedArray) System.out.print(i + " ");
        System.out.println();

        minHeap.buildHeap(unsortedArray);
        System.out.print("After Build Heap: ");
        minHeap.display();

        System.out.println("\n--- Standard Insertion Demo ---");
        minHeap = new MinHeap(5);
        minHeap.insert(3);
        minHeap.insert(2);
        minHeap.insert(15);
        minHeap.display();

        System.out.println("Extract Min: " + minHeap.extractMin());
        minHeap.display();
    }
}
