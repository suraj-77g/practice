package com.notification.channel;

import com.notification.model.NotificationType;
import java.util.HashMap;
import java.util.Map;

public class ChannelFactory {
    private final Map<NotificationType, NotificationChannel> channels = new HashMap<>();

    public ChannelFactory() {
        channels.put(NotificationType.EMAIL, new EmailChannel());
        channels.put(NotificationType.SMS, new SMSChannel());
        channels.put(NotificationType.PUSH, new PushChannel());
    }

    public NotificationChannel getChannel(NotificationType type) {
        NotificationChannel channel = channels.get(type);
        if (channel == null) {
            throw new IllegalArgumentException("No channel found for type: " + type);
        }
        return channel;
    }
}
