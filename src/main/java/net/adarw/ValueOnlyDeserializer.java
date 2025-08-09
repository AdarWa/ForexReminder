package net.adarw;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValueOnlyDeserializer extends JsonDeserializer<Settings> {

    @Override
    public Settings deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        mapper.enable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        JsonNode root = mapper.readTree(p);
        Object cleaned = processNode(root);
        // Directly convert cleaned map to Settings, no reserialization
        return new ObjectMapper().convertValue(cleaned, Settings.class);
    }

    private Object processNode(JsonNode node) {
        if (node.isObject()) {
            // If it contains "value" and optionally "name"/"help", unwrap it
            if (node.has("value") || node.has("name") || node.has("help")) {
                return processNode(node.get("value"));
            }
            if(!node.fields().hasNext()){
                return new TextNode("");
            }
            // Otherwise, process each child
            Map<String, Object> result = new LinkedHashMap<>();
            node.fields().forEachRemaining(field -> {
                result.put(field.getKey(), processNode(field.getValue()));
            });
            return result;
        } else if (node.isArray()) {
            // Process each element of the array
            Object[] arr = new Object[node.size()];
            for (int i = 0; i < node.size(); i++) {
                arr[i] = processNode(node.get(i));
            }
            return arr;
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.booleanValue();
        } else if (node.isTextual()) {
            return node.textValue();
        } else if (node.isNull()) {
            return null;
        }
        return null;
    }
}
