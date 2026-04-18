package com.notification.channel;

import com.notification.model.NotificationRequest;

public interface NotificationChannel {
    void send(NotificationRequest request, String content);
}
