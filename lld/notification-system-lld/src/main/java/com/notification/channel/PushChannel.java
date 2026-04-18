package com.notification.channel;

import com.notification.model.NotificationRequest;

public class PushChannel implements NotificationChannel {
    @Override
    public void send(NotificationRequest request, String content) {
        System.out.println("[PUSH] Sending to token " + request.getRecipient().getPushToken() + ": " + content);
    }
}
