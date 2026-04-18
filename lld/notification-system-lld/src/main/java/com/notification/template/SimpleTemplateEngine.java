package com.notification.template;

import java.util.HashMap;
import java.util.Map;

public class SimpleTemplateEngine implements TemplateEngine {
    private final Map<String, String> templates = new HashMap<>();

    @Override
    public void addTemplate(String templateId, String content) {
        templates.put(templateId, content);
    }

    @Override
    public String render(String templateId, Map<String, Object> data) {
        String template = templates.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }

        String rendered = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
        }
        return rendered;
    }
}
