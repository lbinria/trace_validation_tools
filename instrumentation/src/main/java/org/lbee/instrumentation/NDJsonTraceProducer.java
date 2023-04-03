package org.lbee.instrumentation;

import com.google.gson.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class NDJsonTraceProducer implements TraceProducer {

    private final String guid;
    private final HashSet<TrackedVariable> changes;
    private final List<JsonObject> traces;
    private BufferedWriter writer;

    public String getGuid() { return guid; }

    private String generateTracePath() {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
        return timeStamp + "-" + getGuid() + ".ndjson";
    }

    public NDJsonTraceProducer(String tracePath) {
        // Set unique id
        this.guid = UUID.randomUUID().toString();
        // Get formatted timestamp
        final String path = tracePath != null ? tracePath : generateTracePath();

        this.traces = new ArrayList<>();
        this.changes = new HashSet<>();

        try {
            this.writer = new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NDJsonTraceProducer() {
        this(null);
    }

    @Override
    public void commit(long clock) {

        for (JsonObject trace : this.traces) {
            // Set clock
            trace.addProperty("clock", clock);
            // Commit to file
            try {
                writer.write(trace + "\n");
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }

        // flush
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.traces.clear();
    }

    @Override
    public void commitChanges(long clock) {
        // Trace changes
        for (TrackedVariable changed : changes) {
            trace(changed, clock);
            //changed.setOldValue(serializeValue(changed.getValue()));
        }
        commit(clock);
    }

    public void trace(TrackedVariable variable, long clock)
    {
        // Delta compute
        /*
        JsonElement jsonDeltaValue = computeDelta(variable.getOldValue(), variable.getValue());
        // No changes
        if (jsonDeltaValue == null) {
            System.out.println("NO CHANGES");
            return;
        }
        */

        // Create json object trace
        final JsonObject jsonTrace = new JsonObject();
        jsonTrace.addProperty("sender", this.getGuid());
        jsonTrace.addProperty("var", variable.getName());
        jsonTrace.add("args", serializeValues(variable.getContextArgs()));
        //jsonTrace.add("val", jsonDeltaValue);
        jsonTrace.add("val", serializeValue(variable.getValue()));
        jsonTrace.add("op", new JsonPrimitive("set"));
        jsonTrace.addProperty("clock", clock);

        this.traces.add(jsonTrace);
        System.out.printf("Traced event: %s.\n", jsonTrace);
    }

    @Override
    public void change(TrackedVariable trackedVariable) {
        this.changes.add(trackedVariable);
    }

    @Override
    public void produce(String operator, String variableName, Object[] args) throws TraceProducerException {
            // Create json object trace
            final JsonObject jsonTrace = new JsonObject();
            jsonTrace.addProperty("sender", this.getGuid());
            jsonTrace.addProperty("var", variableName);
            jsonTrace.addProperty("op", operator);
            jsonTrace.add("args", serializeValues(args));

            this.traces.add(jsonTrace);
            System.out.printf("Traced event: %s.\n", jsonTrace);
    }

    public JsonElement serializeValues(Object... values) {

        final JsonArray jsonArgs = new JsonArray();

        for (Object value : values) {
            jsonArgs.add(this.serializeValue(value));
        }

        return jsonArgs;
    }

    public JsonElement serializeValue(Object propertyValue) {
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

    private JsonArray jsonArrayOf(List<?> list) {
        final JsonArray jsonArray = new JsonArray();

        for (Object e : list) {
            jsonArray.add(serializeValue(e));
        }

        return jsonArray;
    }

    private JsonArray jsonArrayOf(HashSet<?> list) {
        final JsonArray jsonArray = new JsonArray();

        for (Object e : list) {
            jsonArray.add(serializeValue(e));
        }

        return jsonArray;
    }

    private JsonArray jsonArrayOf(Object[] array) {
        final JsonArray jsonArray = new JsonArray();

        for (Object e : array) {
            jsonArray.add(serializeValue(e));
        }

        return jsonArray;
    }

    private JsonObject jsonObjectOf(Object object) {
        final JsonObject jsonObject = new JsonObject();

        for (Field field : object.getClass().getDeclaredFields()) {

            if (!field.isAnnotationPresent(TraceField.class))
                continue;

            final TraceField traceField = field.getAnnotation(TraceField.class);

            try {
                field.setAccessible(true);
                final Object fieldValue = field.get(object);
                jsonObject.add(traceField.name(), serializeValue(fieldValue));

            } catch (Exception e) {
                // Nothing
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    private JsonObject jsonObjectOfMap(Map<String,?> map) {
        final JsonObject jsonObject = new JsonObject();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            jsonObject.add(entry.getKey(), serializeValue(entry.getValue()));
        }

        return jsonObject;
    }

    private JsonElement computeDelta(JsonElement oldValue, Object value) {
        //final JsonElement jsonOldValue = oldValue;
        final JsonElement jsonValue = serializeValue(value);
        System.out.println("OLD VALUE: " + oldValue + ", NEW: " + jsonValue);
        return computeJsonElementDelta(oldValue, jsonValue);
    }

    private JsonElement computeJsonElementDelta(JsonElement a, JsonElement b) {

        if (a.isJsonNull() || b.isJsonNull()) {
            JsonObject jsonDelta = new JsonObject();
            jsonDelta.add("set", b);
            return jsonDelta;
        }
        else if (a.isJsonArray() && b.isJsonArray()) {
            return computeJsonArrayDelta(a.getAsJsonArray(), b.getAsJsonArray());
        }
        else {
            JsonObject jsonDelta = new JsonObject();
            jsonDelta.add("set", b);
            return jsonDelta;
        }
    }

    private JsonObject computeJsonArrayDelta(JsonArray a, JsonArray b) {

        if (b.isEmpty()) {
            JsonObject jsonDelta = new JsonObject();
            jsonDelta.add("clear", null);
            return jsonDelta;
        }

        JsonArray deletions = new JsonArray();
        JsonArray adds = new JsonArray();

        for (JsonElement e1 : a) {

            boolean found = false;
            for (JsonElement e2 : b) {
                if (e1.equals(e2)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                deletions.add(e1);
        }

        for (JsonElement e1 : b) {

            boolean found = false;
            for (JsonElement e2 : a) {
                if (e1.equals(e2)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                adds.add(e1);
        }

        if (adds.isEmpty() && deletions.isEmpty())
        {
            return null;
        }

        JsonObject jsonDelta = new JsonObject();
        jsonDelta.add("add", adds);
        jsonDelta.add("del", deletions);
        return jsonDelta;
    }

}
