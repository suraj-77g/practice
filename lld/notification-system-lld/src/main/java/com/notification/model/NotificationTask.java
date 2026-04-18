package com.notification.model;

public class NotificationTask {
    private final NotificationRequest request;
    private final long scheduledTimestamp;

    public NotificationTask(NotificationRequest request) {
        this(request, System.currentTimeMillis());
    }

    public NotificationTask(NotificationRequest request, long scheduledTimestamp) {
        this.request = request;
        this.scheduledTimestamp = scheduledTimestamp;
    }

    public NotificationRequest getRequest() { return request; }
    public long getScheduledTimestamp() { return scheduledTimestamp; }
}
