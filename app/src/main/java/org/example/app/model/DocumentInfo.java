package org.example.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DocumentInfo {

    private String content;
    private Map<String, Object> metadata;

    public String getShortDescription() {
        int maxLength = 100;
        String shortContent = content.length() > maxLength
                ? content.substring(0, maxLength) + "..."
                : content;
        return shortContent.replace("\n", " ");
    }
}
