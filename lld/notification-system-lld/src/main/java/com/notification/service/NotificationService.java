package com.notification.service;

import com.notification.channel.ChannelFactory;
import com.notification.channel.NotificationChannel;
import com.notification.model.NotificationRequest;
import com.notification.template.TemplateEngine;

public class NotificationService {
    private final TemplateEngine templateEngine;
    private final ChannelFactory channelFactory;

    public NotificationService(TemplateEngine templateEngine, ChannelFactory channelFactory) {
        this.templateEngine = templateEngine;
        this.channelFactory = channelFactory;
    }

    public void process(NotificationRequest request) {
        try {
            String content = templateEngine.render(request.getTemplateId(), request.getData());
            NotificationChannel channel = channelFactory.getChannel(request.getType());
            channel.send(request, content);
        } catch (Exception e) {
            System.err.println("Failed to process notification for user " + request.getRecipient().getId() + ": " + e.getMessage());
        }
    }
}
