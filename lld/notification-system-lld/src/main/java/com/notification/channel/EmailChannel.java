package com.notification.channel;

import com.notification.model.NotificationRequest;

public class EmailChannel implements NotificationChannel {
    @Override
    public void send(NotificationRequest request, String content) {
        System.out.println("[EMAIL] Sending to " + request.getRecipient().getEmail() + ": " + content);
    }
}
