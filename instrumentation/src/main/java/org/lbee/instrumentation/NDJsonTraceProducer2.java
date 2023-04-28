package org.lbee.instrumentation;

import com.google.gson.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NDJsonTraceProducer2 implements TraceProducer {

    private final String guid;
    private BufferedWriter writer;

    private final HashMap<String, List<Map.Entry<String, JsonObject>>> updates;

    public String getGuid() { return guid; }

    private String generateTracePath() {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
        return timeStamp + "-" + getGuid() + ".ndjson";
    }

    public NDJsonTraceProducer2(String tracePath) {
        // Set unique id
        this.guid = UUID.randomUUID().toString();
        // Get formatted timestamp
        final String path = tracePath != null ? tracePath : generateTracePath();

        this.updates = new HashMap<>();

        try {
            this.writer = new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NDJsonTraceProducer2() {
        this(null);
    }

    @Override
    public void commit(String description, long clock) {

        final JsonObject jsonEvent = new JsonObject();
        // Set clock
        jsonEvent.addProperty("clock", clock);

        for (Map.Entry<String, List<Map.Entry<String, JsonObject>>> update : this.updates.entrySet()) {
            // Get updated variable and actions made on it
            final String variableName = update.getKey();
            final List<Map.Entry<String, JsonObject>> actions = update.getValue();

            final JsonArray jsonActions = new JsonArray();

            for (Map.Entry<String, JsonObject> action : actions) {
                final JsonObject jsonAction = new JsonObject();
                jsonAction.addProperty("op", action.getKey());
                jsonAction.add("path", action.getValue().getAsJsonArray("path"));
                jsonAction.add("args", action.getValue().getAsJsonArray("args"));
                jsonActions.add(jsonAction);
            }

            jsonEvent.add(variableName, jsonActions);
        }

        // Set description, sender
        jsonEvent.addProperty("desc", description);
        jsonEvent.addProperty("sender", this.getGuid());

        // Commit to file
        try {
            writer.write(jsonEvent + "\n");
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        // flush
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.updates.clear();
    }

    @Override
    public void commitChanges(String description, long clock) {
        commit(description, clock);
    }

    public void trace(TrackedVariable<?> variable, String description, long clock)
    {
    }

    @Override
    public void notifyChange(TrackedVariable<?> trackedVariable) {
    }

    @Override
    public void produce(String operator, String variableName, Object[] args) {
    }


    @Override
    public void addUpdate(String variableName, String action, String[] path, Object[] args) {
        // Create json object trace
        final JsonObject jsonTrace = new JsonObject();
        jsonTrace.add("path", NDJsonSerializer.jsonArrayOf(path));
        jsonTrace.add("args", NDJsonSerializer.serializeValues(args));

        final List<Map.Entry<String, JsonObject>> variableActions;
        if (!updates.containsKey(variableName))
            updates.put(variableName, new ArrayList<>());

        //
        variableActions = updates.get(variableName);
        // Add action to variable
        variableActions.add(Map.entry(action, jsonTrace));
    }

}
