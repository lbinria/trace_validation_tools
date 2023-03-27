package org.lbee.instrumentation;

import com.google.gson.*;
import org.lbee.instrumentation.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class NDJsonTraceProducer implements TraceProducer {

    private final String guid;
    private final List<JsonObject> traces;
    private BufferedWriter writer;

    public String getGuid() { return guid; }

    private String generateTracePath() {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
        return timeStamp + "-" + getGuid() + ".ndjson";
    }

    public NDJsonTraceProducer(String tracePath) {
        // Get formatted timestamp
        final String path = tracePath != null ? tracePath : generateTracePath();

        this.guid = UUID.randomUUID().toString();
        this.traces = new ArrayList<>();

        try {
            this.writer = new BufferedWriter(new FileWriter(tracePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commit(long clock) {

        for (JsonObject trace : this.traces) {
            // Set clock
            trace.addProperty("clock", clock);
            // Commit to file
            try {
                writer.write(trace.toString() + "\n");
            } catch (IOException ex) {

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

    public void rawTrace(Object o) {
//        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create();
//        JsonElement jsonElement = gson.toJsonTree(o);
//        this.traces.add(jsonElement);
    }

    @Override
    public void produce(String operator, String variableName, Object[] args, long clock) throws TraceProducerException {
        try {

            // Create json object trace
            final JsonObject jsonTrace = new JsonObject();
            jsonTrace.addProperty("clock", clock);
            jsonTrace.addProperty("sender", this.getGuid());
            jsonTrace.addProperty("var", variableName);
            jsonTrace.addProperty("op", operator);
            jsonTrace.add("args", serializeValues(args));

            this.traces.add(jsonTrace);
            System.out.printf("Traced event: %s.\n", jsonTrace.toString());

        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // TODO set inner exception in order to keep trace
            throw new TraceProducerException();
        }
    }

    //
    private JsonElement serializeValues(Object... values) throws NoSuchFieldException, IllegalAccessException {

        final JsonArray jsonArgs = new JsonArray();

        for (Object value : values) {
            jsonArgs.add(this.jsonValue(value));
        }

        return jsonArgs;
    }

    private JsonElement jsonValue(Object propertyValue) throws NoSuchFieldException, IllegalAccessException {
        final JsonElement jsonValue;

        if (propertyValue instanceof String)
            jsonValue = new JsonPrimitive((String) propertyValue);
        else if (propertyValue instanceof Boolean)
            jsonValue = new JsonPrimitive((Boolean) propertyValue);
        else if (propertyValue instanceof Number)
            jsonValue = new JsonPrimitive((Number) propertyValue);
        else if (propertyValue instanceof Character)
            jsonValue = new JsonPrimitive((Character) propertyValue);
        else if (propertyValue instanceof Enum)
            jsonValue = new JsonPrimitive(((Enum)propertyValue).ordinal());
        // TODO manage objects
        else
            jsonValue = new JsonPrimitive((String) propertyValue.toString());

        return jsonValue;
    }

}
