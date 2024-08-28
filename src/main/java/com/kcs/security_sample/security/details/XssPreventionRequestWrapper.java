package com.kcs.security_sample.security.details;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XssPreventionRequestWrapper extends HttpServletRequestWrapper {
    private final ObjectMapper objectMapper;
    private Map<String, Object> sanitizedAttributes = new HashMap<>();
    private Map<String, Object> originalAttributes = new HashMap<>();

    public XssPreventionRequestWrapper(HttpServletRequest request, ObjectMapper objectMapper) {
        super(request);
        this.objectMapper = objectMapper;
        sanitizeParameters();
    }

    private void sanitizeParameters() {
        Map<String, String[]> paramMap = super.getParameterMap();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String[] sanitizedValues = new String[entry.getValue().length];
            for (int i = 0; i < entry.getValue().length; i++) {
                sanitizedValues[i] = StringEscapeUtils.escapeHtml4(entry.getValue()[i]);
            }
            sanitizedAttributes.put(entry.getKey(), sanitizedValues.length == 1 ? sanitizedValues[0] : sanitizedValues);
            originalAttributes.put(entry.getKey(), entry.getValue().length == 1 ? entry.getValue()[0] : entry.getValue());
        }
    }

    @Override
    public Object getAttribute(String name) {
        if (name.endsWith("_original")) {
            String originalName = name.substring(0, name.length() - 9);
            return originalAttributes.get(originalName);
        }
        Object sanitizedValue = sanitizedAttributes.get(name);
        return sanitizedValue != null ? sanitizedValue : super.getAttribute(name);
    }

    @Override
    public String getParameter(String name) {
        return StringEscapeUtils.escapeHtml4(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = StringEscapeUtils.escapeHtml4(values[i]);
        }
        return encodedValues;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> paramMap = super.getParameterMap();
        Map<String, String[]> encodedParamMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String[] encodedValues = new String[entry.getValue().length];
            for (int i = 0; i < entry.getValue().length; i++) {
                encodedValues[i] = StringEscapeUtils.escapeHtml4(entry.getValue()[i]);
            }
            encodedParamMap.put(entry.getKey(), encodedValues);
        }
        return encodedParamMap;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String contentType = getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            String body = getRequestBody(super.getReader());
            Map<String, Object> json = objectMapper.readValue(body, Map.class);
            Map<String, Object> sanitizedJson = new HashMap<>(json);
            sanitizeJson(sanitizedJson);
            String sanitizedBody = objectMapper.writeValueAsString(sanitizedJson);

            sanitizedAttributes.putAll(sanitizedJson);
            originalAttributes.putAll(json);

            return new BufferedReader(new StringReader(sanitizedBody));
        }
        return super.getReader();
    }


    private String getRequestBody(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private void sanitizeJson(Map<String, Object> json) {
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            if (entry.getValue() instanceof String) {
                entry.setValue(StringEscapeUtils.escapeHtml4((String) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                sanitizeJson((Map<String, Object>) entry.getValue());
            } else if (entry.getValue() instanceof List) {
                sanitizeList((List<Object>) entry.getValue());
            }
        }
    }

    private void sanitizeList(List<Object> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof String) {
                list.set(i, StringEscapeUtils.escapeHtml4((String) list.get(i)));
            } else if (list.get(i) instanceof Map) {
                sanitizeJson((Map<String, Object>) list.get(i));
            } else if (list.get(i) instanceof List) {
                sanitizeList((List<Object>) list.get(i));
            }
        }
    }
}