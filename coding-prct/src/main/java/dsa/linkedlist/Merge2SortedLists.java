package dsa.linkedlist;

/*
**Trick Summary: Two-Pointer Merge**

**The Algorithm:**
Use two pointers (`ptr1`, `ptr2`) to traverse both sorted lists simultaneously.

**How it works:**
1. Compare values at current positions of both pointers
2. Append the **smaller node** to the result list
3. Advance the pointer of the list whose node was appended
4. Repeat until one list is exhausted
5. Attach remaining nodes from the non-empty list

**Key insight:**
Since both input lists are **already sorted**, you only need a single pass through each. At each step, the smaller value is guaranteed to be the next in the merged sorted order.

**Complexity:**
- **Time:** O(n + m) – linear single pass
- **Space:** O(1) – no extra data structures

**Why it's efficient:**
No sorting needed! You leverage the fact that inputs are pre-sorted to merge in one pass.
* */

public class Merge2SortedLists {

     static class ListNode {
         int val;
         ListNode next;
         ListNode() {}
         ListNode(int val) { this.val = val; }
         ListNode(int val, ListNode next) { this.val = val; this.next = next; }
     }

    static class Solution {

        public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
            ListNode ptr1 = list1;
            ListNode ptr2 = list2;

            ListNode dummy = new ListNode();
            ListNode head = dummy;

            while (ptr1 != null && ptr2 != null) {
                int val1 = ptr1.val;
                int val2 = ptr2.val;

                if (val1 <= val2) {
                    dummy.next = ptr1;
                    ptr1 = ptr1.next;
                } else {
                    dummy.next = ptr2;
                    ptr2 = ptr2.next;
                }
                dummy = dummy.next;
            }

            while (ptr1 != null) {
                dummy.next = ptr1;
                ptr1 = ptr1.next;
                dummy = dummy.next;
            }

            while (ptr2 != null) {
                dummy.next = ptr2;
                ptr2 = ptr2.next;
                dummy = dummy.next;
            }

            return head.next;
        }
    }

}
