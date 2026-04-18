package com.notification.channel;

import com.notification.model.NotificationRequest;

public class SMSChannel implements NotificationChannel {
    @Override
    public void send(NotificationRequest request, String content) {
        System.out.println("[SMS] Sending to " + request.getRecipient().getPhoneNumber() + ": " + content);
    }
}
