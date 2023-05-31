package org.lbee.instrumentation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lbee.instrumentation.clock.InstrumentationClock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

// TODO rename to BehaviorRecorder
public class TraceInstrumentation {

    // Unique id
    private final String guid;
    // Local clock
    private long clock;
    // Global clock
    private final InstrumentationClock globalClock;
    // Writer to write event to file
    private BufferedWriter writer;
    // Updates that happens on a variable
    private final HashMap<String, List<TraceItem>> updates;

    // // Get instrumentation clock
    // public InstrumentationClock getClock() {
    //     return this.clock;
    // }

    // Get instrumentation guid
    public String getGuid() { return guid; }

    // Sync internal instrumentation clock with another clock
    // public void sync(long clock) {
    //     this.clock.sync(clock);
    // }

    private String generateTracePath() {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(Calendar.getInstance().getTime());
        return timeStamp + "-" + getGuid() + ".ndjson";
    }

    public TraceInstrumentation(String tracePath, InstrumentationClock clock) {
        this.clock = 0L;
        this.globalClock = clock;
        // Set unique id
        this.guid = UUID.randomUUID().toString();
        // empty map of variable updates
        this.updates = new HashMap<>();
        // Get formatted timestamp
        final String path = tracePath != null ? tracePath : generateTracePath();
        // Create the file
        // TODO: change to constructor method to return NULL if IO exception
        try {
            this.writer = new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyChange(String variableName, String action, List<String> path, Object... args) {
        // Create json object trace
        List<Object> argsList = Arrays.asList(args);

        if (!updates.containsKey(variableName))
            updates.put(variableName, new ArrayList<>());

        // Add action to variable
        updates.get(variableName).add(new TraceItem(action, path, argsList));
    }

    public VirtualField getVariable(String name) {
        return new VirtualField(name, this);
    }

    public void notifyChange(VirtualUpdate update) {
        notifyChange(update.getVariableName(), update.getOp(), update.getPrefixPath(), update.getArgs());
    }

    public synchronized boolean commitChanges() {
        return commitChanges(null);
    }

    // Note: I found missing synchronized bug thanks to trace validation
    public synchronized boolean commitChanges(String description) {
        // All events are committed at the same logical time (sync)
        // Sync clock
        this.clock = this.globalClock.sync(this.clock);

        // Commit all previously changed variables
        // TODO catch error
        commitChanges(description, this.clock);
        return true;
    }

    private void commitChanges(String description, long clock) {

        final JsonObject jsonEvent = new JsonObject();
        // Set clock
        jsonEvent.addProperty("clock", clock);
        // add actions
        for (String variableName : this.updates.keySet()) {
            // Get actions made on the updated variable
            final List<TraceItem> actions = updates.get(variableName);
            final JsonArray jsonActions = new JsonArray();
            for (TraceItem action : actions) {
                jsonActions.add(action.jsonize());
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
