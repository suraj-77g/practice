package com.notification.service;

import com.notification.model.NotificationRequest;
import com.notification.model.NotificationTask;
import com.notification.queue.NotificationTaskQueue;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationDispatcher {
    private final NotificationTaskQueue taskQueue;
    private final ScheduledExecutorService scheduler;

    public NotificationDispatcher(NotificationTaskQueue taskQueue) {
        this.taskQueue = taskQueue;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void dispatch(NotificationRequest request) {
        try {
            taskQueue.enqueue(new NotificationTask(request));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to dispatch notification: " + e.getMessage());
        }
    }

    public void dispatchBulk(List<NotificationRequest> requests) {
        for (NotificationRequest request : requests) {
            dispatch(request);
        }
    }

    public void schedule(NotificationRequest request, long delay, TimeUnit unit) {
        scheduler.schedule(() -> {
            try {
                System.out.println("[SCHEDULER] Enqueueing scheduled notification after delay.");
                taskQueue.enqueue(new NotificationTask(request));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Failed to enqueue scheduled notification: " + e.getMessage());
            }
        }, delay, unit);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
