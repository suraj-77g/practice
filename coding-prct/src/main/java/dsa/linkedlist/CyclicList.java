package dsa.linkedlist;

public class CyclicList {

     public static class ListNode {
         int val;
         ListNode next;
         ListNode(int x) {
             val = x;
             next = null;
         }
     }

    public static class Solution {
        public boolean hasCycle(ListNode head) {
            ListNode curr = head;

            ListNode slowPtr = head;
            ListNode fastPtr = head;

            while (fastPtr != null && fastPtr.next != null) {
                slowPtr = slowPtr.next;
                fastPtr = fastPtr.next.next;

                if (slowPtr == fastPtr)
                    return true;
            }

            return false;
        }
    }

}
