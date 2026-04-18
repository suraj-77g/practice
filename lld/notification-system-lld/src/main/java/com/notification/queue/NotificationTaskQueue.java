package com.notification.queue;

import com.notification.model.NotificationTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NotificationTaskQueue {
    private final BlockingQueue<NotificationTask> queue;

    public NotificationTaskQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public void enqueue(NotificationTask task) throws InterruptedException {
        // This will block if the queue is full, providing backpressure
        queue.put(task);
    }

    public NotificationTask dequeue() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }
}
