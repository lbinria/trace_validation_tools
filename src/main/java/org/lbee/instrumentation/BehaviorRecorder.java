package org.lbee.instrumentation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lbee.instrumentation.clock.InstrumentationClock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BehaviorRecorder {

    // Unique id
    private final String guid;
    // Local clock
    private long clock;
    // Global clock
    private final InstrumentationClock globalClock;
    // Writer to write event to file
    private final BufferedWriter writer;
    // Updates that happens on a variable
    private final HashMap<String, List<TraceItem>> updates;

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

    protected BehaviorRecorder(BufferedWriter writer, InstrumentationClock clock) {
        this.clock = 0L;
        this.globalClock = clock;
        // Set unique id
        this.guid = UUID.randomUUID().toString();
        // Empty map of variable updates
        this.updates = new HashMap<>();
        // Set writer
        this.writer = writer;
    }

    /**
     * Create new instrumentation
     * @param tracePath Path of file where the trace will be recorded
     * @param clock Clock used for sync
     * @return A new instrumentation or null if unable to create the trace file
     */
    public static BehaviorRecorder create(String tracePath, InstrumentationClock clock) {
        try {
            // Create the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(tracePath));
            return new BehaviorRecorder(writer, clock);
        } catch (IOException e) {
            return null;
        }
    }

    public void notifyChange(String variableName, String action, List<String> path, List<Object> args) {

        if (!updates.containsKey(variableName))
            updates.put(variableName, new ArrayList<>());

        // Add action to variable
        updates.get(variableName).add(new TraceItem(action, path, args));
    }

    public VirtualField getVariable(String name) {
        return new VirtualField(name, this);
    }

    public void notifyChange(VirtualUpdate update) {
        notifyChange(update.getVariableName(), update.getOp(), update.getPrefixPath(), update.getArgs());
    }

    public void commitChanges() throws IOException {
        commitChanges("");
    }

    /**
     * Commit an exception catch from implementation
     * @param desc Description of the exception
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void commitException(String desc) throws IOException {
        commitChanges("__exception", desc);
    }

    public void commitChanges(String eventName) throws IOException {
        commitChanges(eventName, "");
    }

    public void commitChanges(String eventName, Object[] args) throws IOException {
        commitChanges(eventName, args, "");
    }

    public void commitChanges(String eventName, String desc) throws IOException {
        commitChanges(eventName, new Object[] {}, desc);
    }

    // Note: I found missing synchronized bug thanks to trace validation
    public synchronized void commitChanges(String eventName, Object[] args, String desc) throws IOException {
        // All events are committed at the same logical time (sync)
        // Sync clock
        this.clock = this.globalClock.sync(this.clock);
        // Commit all previously changed variables
        commitChanges(eventName, desc, args, this.clock);
    }


    private void commitChanges(String eventName, String desc, Object[] args, long clock) throws IOException {

        final JsonObject jsonEvent = new JsonObject();
        // Set clock
        jsonEvent.addProperty("clock", clock);

        try {
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

            // Set description if filled
            if (eventName != null && !eventName.equals(""))
                jsonEvent.addProperty("event", eventName);

            // Set desc if filled
            if (desc != null && !desc.equals(""))
                jsonEvent.addProperty("desc", desc);

            if (args != null && args.length > 0)
                jsonEvent.add("event_args", NDJsonSerializer.jsonArrayOf(args));

        } catch (IllegalAccessException e) {
            // Set event to exception, fill description with exception message
            eventName = "__exception";
            desc = e.toString();
        }

        // Set sender
        jsonEvent.addProperty("sender", this.getGuid());

        // Commit to file
        writer.write(jsonEvent + "\n");
        writer.flush();

        this.updates.clear();
    }

}
