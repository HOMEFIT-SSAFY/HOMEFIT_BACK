package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;

public class OpenApiJsonParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<JsonNode> items(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            List<JsonNode> detailItems = mergedDetailScheduleItems(root);
            if (!detailItems.isEmpty()) {
                return detailItems;
            }
            JsonNode node = itemNode(root);
            return normalizeItems(node);
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<JsonNode> mergedDetailScheduleItems(JsonNode root) {
        if (!root.isArray()) {
            return List.of();
        }
        JsonNode schedule = null;
        JsonNode site = null;
        for (JsonNode child : root) {
            if (schedule == null) {
                schedule = firstArrayItem(child.path("dsSplScdl"));
            }
            if (site == null) {
                site = firstArrayItem(child.path("dsSbd"));
            }
        }
        if (schedule == null && site == null) {
            return List.of();
        }
        com.fasterxml.jackson.databind.node.ObjectNode merged = objectMapper.createObjectNode();
        if (schedule != null) {
            merged.setAll((com.fasterxml.jackson.databind.node.ObjectNode) schedule);
        }
        if (site != null) {
            merged.setAll((com.fasterxml.jackson.databind.node.ObjectNode) site);
        }
        return List.of(merged);
    }

    private JsonNode firstArrayItem(JsonNode node) {
        if (node.isArray() && !node.isEmpty() && node.get(0).isObject()) {
            return node.get(0);
        }
        return null;
    }

    private JsonNode itemNode(JsonNode root) {
        JsonNode mergedDetailNode = mergedLhDetailNode(root);
        if (mergedDetailNode != null) {
            return mergedDetailNode;
        }

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

    private JsonNode mergedLhDetailNode(JsonNode root) {
        if (!root.isArray()) {
            return null;
        }
        ObjectNode merged = objectMapper.createObjectNode();
        mergeFirstObject(root, merged, "dsSplScdl");
        mergeFirstObject(root, merged, "dsSbd");
        mergeFirstObject(root, merged, "dsEtcInfo");
        return merged.isEmpty() ? null : merged;
    }

    private void mergeFirstObject(JsonNode root, ObjectNode merged, String fieldName) {
        for (JsonNode child : root) {
            JsonNode node = child.path(fieldName);
            if (node.isArray() && !node.isEmpty() && node.get(0).isObject()) {
                merged.setAll((ObjectNode) node.get(0));
                return;
            }
            if (node.isObject()) {
                merged.setAll((ObjectNode) node);
                return;
            }
        }
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

