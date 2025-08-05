package net.adarw;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class RecursiveSettingsDeserializer extends JsonDeserializer<Settings> {

    private final Class<Settings> targetClass;

    public RecursiveSettingsDeserializer(Class<Settings> targetClass) {
        this.targetClass = targetClass;
    }

    public RecursiveSettingsDeserializer() {
        this.targetClass = Settings.class; // no-arg constructor for Jackson
    }

    @Override
    public Settings deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode rootNode = mapper.readTree(p);
        Object obj = deserializeNode(rootNode, targetClass, mapper);
        return targetClass.cast(obj);
    }

    private Object deserializeNode(JsonNode node, Class<?> clazz, ObjectMapper mapper) throws IOException {
        if (node == null || node.isNull()) return null;

        // Handle primitives and wrappers, String
        if (clazz.isPrimitive() || clazz == String.class
                || Number.class.isAssignableFrom(clazz) || clazz == Boolean.class) {
            JsonNode valueNode = node.has("value") ? node.get("value") : node;
            return mapper.treeToValue(valueNode, clazz);
        }

        // Handle Collections
        if (Collection.class.isAssignableFrom(clazz)) {
            if (!node.has("value") || !node.get("value").isArray()) {
                return Collections.emptyList();
            }

            JsonNode arrayNode = node.get("value");
            Collection<Object> collection;

            if (clazz.isInterface()) {
                collection = new ArrayList<>();
            } else {
                try {
                    collection = (Collection<Object>) clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    collection = new ArrayList<>();
                }
            }

            // Element type fallback
            Class<?> elementType = Object.class;

            // Note: Since we don't have Field context here, we can't reflect element type,
            // you might need to pass it as argument or hardcode known lists if needed

            for (JsonNode itemNode : arrayNode) {
                Object element = deserializeNode(itemNode, elementType, mapper);
                collection.add(element);
            }
            return collection;
        }

        // Handle arrays
        if (clazz.isArray()) {
            if (!node.has("value") || !node.get("value").isArray()) {
                return Array.newInstance(clazz.getComponentType(), 0);
            }

            JsonNode arrayNode = node.get("value");
            int length = arrayNode.size();
            Object array = Array.newInstance(clazz.getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Object element = deserializeNode(arrayNode.get(i), clazz.getComponentType(), mapper);
                Array.set(array, i, element);
            }
            return array;
        }

        // Handle POJOs and nested classes
        Object instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IOException("Failed to instantiate " + clazz, e);
        }

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String jsonFieldName = entry.getKey();
            JsonNode valueWrapperNode = entry.getValue();

            try {
                Field field = clazz.getDeclaredField(jsonFieldName);
                field.setAccessible(true);

                Class<?> fieldType = field.getType();

                JsonNode valueNode = valueWrapperNode.has("value") ? valueWrapperNode.get("value") : valueWrapperNode;

                if (Collection.class.isAssignableFrom(fieldType)) {
                    Class<?> elementType = Object.class;
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
                        if (typeArgs.length == 1 && typeArgs[0] instanceof Class) {
                            elementType = (Class<?>) typeArgs[0];
                        }
                    }

                    Collection<Object> collection;
                    if (fieldType.isInterface()) {
                        collection = new ArrayList<>();
                    } else {
                        try {
                            collection = (Collection<Object>) fieldType.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            collection = new ArrayList<>();
                        }
                    }

                    if (valueNode.isArray()) {
                        for (JsonNode itemNode : valueNode) {
                            Object element = deserializeNode(itemNode, elementType, mapper);
                            collection.add(element);
                        }
                    }

                    field.set(instance, collection);
                } else {
                    Object fieldValue = deserializeNode(valueWrapperNode, fieldType, mapper);
                    field.set(instance, fieldValue);
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Field not found or inaccessible; ignore or log as needed
            }
        }
        return instance;
    }
}
