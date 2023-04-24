package org.lbee.instrumentation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class NDJsonSerializer {

    static JsonElement serializeValues(Object... values) {

        final JsonArray jsonArgs = new JsonArray();

        for (Object value : values) {
            jsonArgs.add(serializeValue(value));
        }

        return jsonArgs;
    }

    static JsonElement serializeValue(Object propertyValue) {
        final JsonElement jsonValue;

        if (propertyValue == null)
            return null;
        else if (propertyValue instanceof String)
            jsonValue = new JsonPrimitive((String) propertyValue);
        else if (propertyValue instanceof Boolean)
            jsonValue = new JsonPrimitive((Boolean) propertyValue);
        else if (propertyValue instanceof Number)
            jsonValue = new JsonPrimitive((Number) propertyValue);
        else if (propertyValue instanceof Character)
            jsonValue = new JsonPrimitive((Character) propertyValue);
        else if (propertyValue instanceof Enum)
            jsonValue = new JsonPrimitive(((Enum)propertyValue).ordinal());
        else if (propertyValue instanceof Object[])
            jsonValue = jsonArrayOf((Object[]) propertyValue);
        else if (propertyValue instanceof List<?>)
            jsonValue = jsonArrayOf((List<?>)propertyValue);
        else if (propertyValue instanceof HashSet<?>)
            jsonValue = jsonArrayOf((HashSet<?>)propertyValue);
        else if (propertyValue instanceof Map<?,?>)
            jsonValue = jsonObjectOfMap((Map<String, ?>) propertyValue);
        else
            jsonValue = jsonObjectOf(propertyValue);

        return jsonValue;
    }

    static JsonArray jsonArrayOf(List<?> list) {
        final JsonArray jsonArray = new JsonArray();

        for (Object e : list) {
            jsonArray.add(serializeValue(e));
        }

        return jsonArray;
    }

    static JsonArray jsonArrayOf(HashSet<?> list) {
        final JsonArray jsonArray = new JsonArray();

        for (Object e : list) {
            jsonArray.add(serializeValue(e));
        }

        return jsonArray;
    }

    static JsonArray jsonArrayOf(Object[] array) {
        final JsonArray jsonArray = new JsonArray();

        for (Object e : array) {
            jsonArray.add(serializeValue(e));
        }

        return jsonArray;
    }

    static JsonObject jsonObjectOf(Object object) {
        final JsonObject jsonObject = new JsonObject();

        for (Field field : object.getClass().getDeclaredFields()) {

            if (!field.isAnnotationPresent(TraceField.class))
                continue;

            final TraceField traceField = field.getAnnotation(TraceField.class);

            try {
                field.setAccessible(true);
                final String fieldName = traceField.name() != null && !traceField.name().equals("") ? traceField.name() : field.getName();
                final Object fieldValue = field.get(object);
                jsonObject.add(fieldName, serializeValue(fieldValue));

            } catch (Exception e) {
                // Nothing
                e.printStackTrace();
            }
        }

        // Add field containing class name of the object
        jsonObject.addProperty("__type", object.getClass().getName());

        return jsonObject;
    }

    static JsonObject jsonObjectOfMap(Map<String, ?> map) {
        final JsonObject jsonObject = new JsonObject();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            jsonObject.add(entry.getKey(), serializeValue(entry.getValue()));
        }

        return jsonObject;
    }

}
