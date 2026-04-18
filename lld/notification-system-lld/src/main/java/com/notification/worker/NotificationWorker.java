package com.notification.worker;

import com.notification.model.NotificationTask;
import com.notification.queue.NotificationTaskQueue;
import com.notification.service.NotificationService;

public class NotificationWorker implements Runnable {
    private final NotificationTaskQueue queue;
    private final NotificationService notificationService;
    private volatile boolean running = true;

    public NotificationWorker(NotificationTaskQueue queue, NotificationService notificationService) {
        this.queue = queue;
        this.notificationService = notificationService;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " started.");
        while (running || !Thread.currentThread().isInterrupted()) {
            try {
                NotificationTask task = queue.dequeue();
                notificationService.process(task.getRequest());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing notification: " + e.getMessage());
            }
        }
        System.out.println(Thread.currentThread().getName() + " stopped.");
    }

    public void stop() {
        this.running = false;
    }
}
