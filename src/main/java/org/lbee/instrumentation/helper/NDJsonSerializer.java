package org.lbee.instrumentation.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A helper class for serializing objects to NDJSON. The class provides methods
 * for serializing values of basic types, arrays, and maps to JSON elements. It
 * also accepts objects that implement the {@link TLASerializer} interface.
 */
public class NDJsonSerializer {
    /**
     * Serializes the given values to a JSON array.
     */
    static JsonElement serializeValues(Object... values) throws IllegalAccessException {
        final JsonArray jsonArgs = new JsonArray();
        for (Object value : values) {
            jsonArgs.add(serializeValue(value));
        }
        return jsonArgs;
    }

    /**
     * Serializes the given value to a JSON element.
     */
    static JsonElement serializeValue(Object propertyValue) throws IllegalAccessException {
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
        else if (propertyValue instanceof Enum<?>)
            jsonValue = new JsonPrimitive(((Enum<?>) propertyValue).ordinal());
        else if (propertyValue instanceof Object[])
            jsonValue = jsonArrayOf((Object[]) propertyValue);
        else if (propertyValue instanceof List<?>)
            jsonValue = jsonArrayOf((List<?>) propertyValue);
        else if (propertyValue instanceof HashSet<?>)
            jsonValue = jsonArrayOf((HashSet<?>) propertyValue);
        else if (propertyValue instanceof Map<?, ?>)
            jsonValue = jsonObjectOfMap((Map<?, ?>) propertyValue);
        else if (propertyValue instanceof TLASerializer)
            jsonValue = ((TLASerializer) propertyValue).tlaSerialize();
        else
            throw new IllegalAccessException("Unknown");
        return jsonValue;
    }

    /**
     * Serializes the given list to a JSON array.
     */
    public static JsonArray jsonArrayOf(List<?> list) throws IllegalAccessException {
        final JsonArray jsonArray = new JsonArray();
        for (Object e : list) {
            jsonArray.add(serializeValue(e));
        }
        return jsonArray;
    }

    /**
     * Serializes the given set to a JSON array.
     */
    public static JsonArray jsonArrayOf(HashSet<?> list) throws IllegalAccessException {
        final JsonArray jsonArray = new JsonArray();
        for (Object e : list) {
            jsonArray.add(serializeValue(e));
        }
        return jsonArray;
    }

    /**
     * Serializes the given array to a JSON array.
     */
    public static JsonArray jsonArrayOf(Object[] array) throws IllegalAccessException {
        final JsonArray jsonArray = new JsonArray();
        for (Object e : array) {
            jsonArray.add(serializeValue(e));
        }
        return jsonArray;
    }

    /**
     * Serializes the given map to a JSON object.
     */
    static JsonObject jsonObjectOfMap(Map<?, ?> map) throws IllegalAccessException {
        final JsonObject jsonObject = new JsonObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            jsonObject.add(entry.getKey().toString(), serializeValue(entry.getValue()));
        }
        return jsonObject;
    }
}
