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
    // TODO move to TraceInstrumentation
    private final HashSet<TrackedVariable<?>> changes;
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
        for (TrackedVariable<?> changed : changes) {
            trace(changed, clock);
        }
        this.changes.clear();
        commit(clock);
    }

    public void trace(TrackedVariable<?> variable, long clock)
    {
        // Create json object trace
        final JsonObject jsonTrace = new JsonObject();
        jsonTrace.addProperty("clock", clock);
        jsonTrace.addProperty("var", variable.getName());
        jsonTrace.add("ctx", NDJsonSerializer.serializeValues(variable.getContextArgs()));
        jsonTrace.add("args", NDJsonSerializer.serializeValue(variable.getValue()));
        jsonTrace.add("op", new JsonPrimitive("set"));
        jsonTrace.addProperty("sender", this.getGuid());

        this.traces.add(jsonTrace);
        System.out.printf("Traced event: %s.\n", jsonTrace);
    }

    @Override
    public void notifyChange(TrackedVariable<?> trackedVariable) {
        this.changes.add(trackedVariable);
    }

    @Override
    public void produce(String operator, String variableName, Object[] args) {
            // Create json object trace
            final JsonObject jsonTrace = new JsonObject();
            jsonTrace.addProperty("sender", this.getGuid());
            jsonTrace.addProperty("var", variableName);
            jsonTrace.addProperty("op", operator);
            jsonTrace.add("args", NDJsonSerializer.serializeValues(args));

            this.traces.add(jsonTrace);
            System.out.printf("Traced event: %s.\n", jsonTrace);
    }


    @Override
    public void addUpdate(String variableName, String action, String[] path, Object[] args) {
        // Create json object trace
        final JsonObject jsonTrace = new JsonObject();
        jsonTrace.addProperty("sender", this.getGuid());
        jsonTrace.addProperty("var", variableName);
        jsonTrace.addProperty("op", action);
        jsonTrace.add("path", NDJsonSerializer.jsonArrayOf(path));
        jsonTrace.add("args", NDJsonSerializer.serializeValues(args));
        this.traces.add(jsonTrace);
    }

}
