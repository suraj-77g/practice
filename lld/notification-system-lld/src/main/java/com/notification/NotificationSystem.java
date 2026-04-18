package com.notification;

import com.notification.model.NotificationRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface NotificationSystem {
    /**
     * Sends a single notification asynchronously.
     */
    void send(NotificationRequest request);

    /**
     * Sends multiple notifications asynchronously.
     */
    void sendBulk(List<NotificationRequest> requests);

    /**
     * Schedules a notification to be sent after a specified delay.
     */
    void schedule(NotificationRequest request, long delay, TimeUnit unit);

    /**
     * Gracefully shuts down the notification system.
     */
    void shutdown();
}
