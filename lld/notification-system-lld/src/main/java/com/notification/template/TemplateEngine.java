package com.notification.template;

import java.util.Map;

public interface TemplateEngine {
    String render(String templateId, Map<String, Object> data);
    void addTemplate(String templateId, String content);
}
