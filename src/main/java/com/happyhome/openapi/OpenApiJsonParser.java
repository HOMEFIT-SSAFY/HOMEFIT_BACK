package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class OpenApiJsonParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<JsonNode> items(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = itemNode(root);
            return normalizeItems(node);
        } catch (Exception e) {
            return List.of();
        }
    }

    private JsonNode itemNode(JsonNode root) {
        JsonNode node = firstExisting(
                root.path("dsList"),
                root.path("response").path("data"),
                root.path("response").path("body").path("items").path("item"),
                root.path("body").path("items"),
                root.path("items")
        );
        if (node != null) {
            return node;
        }
        node = numberedDsListNode(root);
        if (node != null) {
            return node;
        }
        if (root.isArray()) {
            for (JsonNode child : root) {
                JsonNode childNode = itemNode(child);
                if (childNode != null) {
                    return childNode;
                }
            }
        }
        return null;
    }

    private JsonNode numberedDsListNode(JsonNode root) {
        if (!root.isObject()) {
            return null;
        }
        java.util.Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (fieldName.startsWith("dsList") && !fieldName.endsWith("Nm")) {
                JsonNode node = root.path(fieldName);
                if (!node.isMissingNode() && !node.isNull()) {
                    return node;
                }
            }
        }
        return null;
    }

    private JsonNode firstExisting(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (!node.isMissingNode() && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private List<JsonNode> normalizeItems(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        if (node.isArray()) {
            List<JsonNode> result = new ArrayList<>();
            node.forEach(result::add);
            return result;
        }
        if (node.isObject()) {
            return List.of(node);
        }
        return List.of();
    }
}

