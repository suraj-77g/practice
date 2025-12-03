package com.company.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpHeaderParser {

    public static void main(String[] args) {
        String headers = "Accept-Language: en-US,en;q=0.5\n" +
                "Content-Type: application/json\n" +
                "Content-Type: application/xml\n" +
                " X-Custom-Header :   some_value";

        Map<String, Object> headerMap = parseHeaders(headers);
        for (Map.Entry<String, Object> entry:  headerMap.entrySet()) {
            System.out.println(entry);
        }
    }

    private static Map<String, Object> parseHeaders(String headers) {
        String[] lines = headers.split("\n");
        Map<String, Object> headerMap = new HashMap<>();

        for (String line : lines) {
            String[] parts = line.split(":", 2);
            if (parts.length < 2)
                continue;

            String keyRaw = parts[0];
            String key = keyRaw.trim().toLowerCase();

            String valueRaw = parts[1];
            String value = valueRaw.trim();

            if (!headerMap.containsKey(key)) {
                headerMap.put(key, value);
            } else {
                Object existingValue = headerMap.get(key);
                if (existingValue == null) {
                    headerMap.put(key, new ArrayList<>(List.of("", value)));
                } else if (existingValue instanceof String) {
                    String existingStrVal = (String) existingValue;
                    List<String> values = new ArrayList<>(List.of(existingStrVal, value));
                    headerMap.put(key, values);
                } else if (existingValue instanceof List) {
                    List<String> values = (List) existingValue;
                    values.add(value);
                }
            }
        }
        return headerMap;
    }

}