package com.notification.model;

import java.util.Map;

public class NotificationRequest {
    private final User recipient;
    private final NotificationType type;
    private final String templateId;
    private final Map<String, Object> data;

    public NotificationRequest(User recipient, NotificationType type, String templateId, Map<String, Object> data) {
        this.recipient = recipient;
        this.type = type;
        this.templateId = templateId;
        this.data = data;
    }

    public User getRecipient() { return recipient; }
    public NotificationType getType() { return type; }
    public String getTemplateId() { return templateId; }
    public Map<String, Object> getData() { return data; }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "recipient=" + recipient +
                ", type=" + type +
                ", templateId='" + templateId + '\'' +
                '}';
    }
}
