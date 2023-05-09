package org.lbee.instrumentation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lbee.instrumentation.clock.InstrumentationClock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

// TODO rename to EventRecorder
public class TraceInstrumentation {

    // Unique id
    private final String guid;
    // Local clock
    private final InstrumentationClock clock;
    // Instrumented values
    //private final ArrayList<TrackedVariable<?>> variables;
    // Writer to write event to file
    private BufferedWriter writer;
    // Updates that happens on a variable
    private final HashMap<String, List<Map.Entry<String, JsonObject>>> updates;

    // Get instrumentation clock
    public InstrumentationClock getClock() {
        return this.clock;
    }

    // Get instrumentation guid
    public String getGuid() { return guid; }

    // Sync internal instrumentation clock with another clock
    public void sync(long clock) {
        this.clock.sync(clock);
    }

    private String generateTracePath() {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
        return timeStamp + "-" + getGuid() + ".ndjson";
    }

    public TraceInstrumentation(String tracePath, InstrumentationClock clock) {
//        this.variables = new ArrayList<>();
        this.clock = clock;
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

//    public <TValue> TrackedVariable<TValue> add(String name, TValue value) {
//        return add(name, value, new Object[] {});
//    }

    /*
    public <TValue> TrackedVariable<TValue> add(String name, TValue value, Object... contextArgs) {
        final TrackedVariable<TValue> trackedVariable = new TrackedVariable<>(name, value, this.traceProducer, contextArgs);
        this.variables.add(trackedVariable);
        return trackedVariable;
    }
    */

    public void notifyChange(String variableName, String action, String[] path, Object[] args) {
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

//    /**
//     * Get a tracked variable by name
//     * @param name Tracked variable name
//     * @return A tracked variable
//     */
//    public TrackedVariable<?> get(String name) {
//        return this.variables.get(name);
//    }

    public synchronized boolean commitChanges() {
        return commitChanges(null);
    }

    // Note: I found missing synchronized bug thanks to trace validation
    public synchronized boolean commitChanges(String description) {
        // All events are committed at the same logical time (sync)
        // Sync clock
        final long clock = this.clock.sync(this.clock.getValue());

        // Commit all previously changed variables
        // TODO catch error
        commitChanges(description, clock);
        return true;
    }

    private void commitChanges(String description, long clock) {

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

}
