package org.lbee.instrumentation.trace;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lbee.instrumentation.clock.InstrumentationClock;
import org.lbee.instrumentation.helper.NDJsonSerializer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TLATracer {

    // Unique id
    private final String guid;
    // Local clock
    private long clockValue;
    // Global clock
    private final InstrumentationClock clock;
    // Writer that write event to file
    private final BufferedWriter writer;
    // Updates that happens on a variable (modifications batch)
    private final HashMap<String, List<TraceItem>> updates;

    /**
     * Get instrumentation guid
     * 
     * @return Unique id of instrumentation
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Construct a new instrumentation
     * 
     * @param writer The buffer that write trace to an output stream
     * @param clock  The clock used when logging
     */
    private TLATracer(BufferedWriter writer, InstrumentationClock clock) {
        // this.clock = -1;
        this.clock = clock;
        // Set unique id
        this.guid = UUID.randomUUID().toString();
        // Empty map of variable updates
        this.updates = new HashMap<>();
        // Set writer
        this.writer = writer;
        this.clockValue = this.clock.sync(-1);
    }

    /**
     * Create new instrumentation
     * 
     * @param tracePath Path of file where the trace will be recorded
     * @param clock     Clock used for sync
     * @return A new instrumentation or null if unable to create the trace file
     */
    public static TLATracer getTracer(String tracePath, InstrumentationClock clock) {
        try {
            // Create the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(tracePath));
            return new TLATracer(writer, clock);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Notify the modification of the value of a variable
     * 
     * @param variableName The name of the variable that is modified
     * @param operator     Operator applied to the variable
     * @param path         Path of the variable that is modified (e.g: 'firstname'
     *                     for a record person)
     * @param args         Arguments used in operator
     */
    public void notifyChange(String variableName, String operator, List<String> path, List<Object> args) {
        if (!updates.containsKey(variableName)) {
            updates.put(variableName, new ArrayList<>());
        }
        // Add action to variable
        updates.get(variableName).add(new TraceItem(operator, path, args));
    }

    /**
     * Commit an exception catch from implementation
     * 
     * @param desc Description of the exception
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void logException(String desc) throws IOException {
        this.log("__exception", desc);
    }

    /**
     * Get a virtual variable, that serve to notify change later
     * 
     * @param variableName Name of the variable
     * @return A virtual variable on which you can notify changes
     */
    public VirtualField getVariableTracer(String variableName) {
        return new VirtualField(variableName, this);
    }

    /**
     * Commit all variable changes in batch
     * 
     * @param eventName Name of the event that is committed (may correspond to
     *                  action name in TLA+ for example)
     * @param desc      Description of the commit (custom message)
     * @param args      Arguments of the event that is committed (may correspond to
     *                  action arguments in TLA+ for example)
     * @param clock     Instrumentation current clock value
     * @throws IOException Thrown when unable to write event in trace file
     */
    private void logChanges(String eventName, Object[] args, String desc, long clock) throws IOException {
        final JsonObject jsonEvent = new JsonObject();
        // Set clock
        jsonEvent.addProperty("clock", clock);
        try {
            // add actions
            for (String variableName : this.updates.keySet()) {
                // Get actions made on the updated variable
                final List<TraceItem> actions = this.updates.get(variableName);
                final JsonArray jsonActions = new JsonArray();
                for (TraceItem action : actions) {
                    jsonActions.add(action.jsonize());
                }
                jsonEvent.add(variableName, jsonActions);
            }

            // Set eventName if filled
            if (eventName != null && !eventName.equals("")) {
                jsonEvent.addProperty("event", eventName);
            }
            // Set desc if filled
            if (desc != null && !desc.equals("")) {
                jsonEvent.addProperty("desc", desc);
            }
            if (args != null && args.length > 0) {
                jsonEvent.add("event_args", NDJsonSerializer.jsonArrayOf(args));
            }
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
        // clear for next log
        this.updates.clear();
    }

    /**
     * Commit all variable changes in batch
     * 
     * @param eventName  Name of the event that is committed (may correspond to
     *                   action name in TLA+ for example)
     * @param args       Arguments of the event that is committed (may correspond to
     *                   action arguments in TLA+ for example)
     * @param clockValue the current clockValue of the process at the moment the log
     *                   is done
     * @param desc       Description of the commit (custom message)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void log(String eventName, Object[] args, long clockValue, String desc) throws IOException {
        // Update local clock
        this.clockValue = this.clock.sync(clockValue);
        // Commit all previously changed variables
        this.logChanges(eventName, args, desc, this.clockValue);
    }

    /**
     * Commit all variable changes in batch
     * 
     * @param eventName Name of the event that is committed (may correspond to
     *                  action name in TLA+ for example)
     * @param args      Arguments of the event that is committed (may correspond to
     *                  action arguments in TLA+ for example)
     * @param desc      Description of the commit (custom message)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void log(String eventName, Object[] args, String desc) throws IOException {
        this.log(eventName, args, 0L, desc);
    }

    /**
     * Commit all variable changes in batch
     * 
     * @param eventName Name of the event that is committed (may correspond to
     *                  action name in TLA+ for example)
     * @param desc      Description of the commit (custom message)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void log(String eventName, String desc) throws IOException {
        this.log(eventName, new Object[] {}, desc);
    }

    /**
     * Commit all variable changes in batch
     * 
     * @param eventName Name of the event that is committed (may correspond to
     *                  action name in TLA+ for example)
     * @param args      Arguments of the event that is committed (may correspond to
     *                  action arguments in TLA+ for example)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void log(String eventName, Object[] args) throws IOException {
        this.log(eventName, args, "");
    }

    /**
     * Commit all variable changes in batch
     * 
     * @param eventName Name of the event that is committed (may correspond with
     *                  action name in TLA+ for example)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void log(String eventName) throws IOException {
        this.log(eventName, "");
    }

    /**
     * Commit changes without specifying event name
     * 
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void log() throws IOException {
        this.log("");
    }
}
