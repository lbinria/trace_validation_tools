package org.lbee.instrumentation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.lbee.instrumentation.clock.InstrumentationClock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BehaviorRecorder {

    // Unique id
    private final String guid;
    // Local clock
    private long clock;
    // Global clock
    private final InstrumentationClock globalClock;
    // Writer that write event to file
    private final BufferedWriter writer;
    // Updates that happens on a variable (modifications batch)
    private final HashMap<String, List<TraceItem>> updates;

    /**
     * Get instrumentation guid
     * @return Unique id of instrumentation
     */
    public String getGuid() { return guid; }

    // Sync internal instrumentation clock with another clock
    // public void sync(long clock) {
    //     this.clock.sync(clock);
    // }

    /**
     * Construct a new instrumentation
     * @param writer The buffer that write trace to an output stream
     * @param clock The clock used when logging
     */
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

    /**
     * Notify the modification of the value of a variable
     * @param variableName The name of the variable that is modified
     * @param operator Operator applied to the variable
     * @param path Path of the variable that is modified (e.g: 'firstname' for a record person)
     * @param args Arguments used in operator
     */
    public void notifyChange(String variableName, String operator, List<String> path, List<Object> args) {

        if (!updates.containsKey(variableName))
            updates.put(variableName, new ArrayList<>());

        // Add action to variable
        updates.get(variableName).add(new TraceItem(operator, path, args));
    }

    /**
     * Get a virtual variable, that serve to notify change later
     * @param name Name of the variable
     * @return A virtual variable on which you can notify changes
     */
    public VirtualField getVariable(String name) {
        return new VirtualField(name, this);
    }

    /**
     * Notify the modification of the value of a variable
     * @param update The virtual update to notify
     */
    public void notifyChange(VirtualUpdate update) {
        notifyChange(update.getVariableName(), update.getOp(), update.getPrefixPath(), update.getArgs());
    }

    /**
     * Commit changes without specifying event name
     * @throws IOException Thrown when unable to write event in trace file
     */
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

    /**
     * Commit all variable changes in batch
     * @param eventName Name of the event that is committed (may correspond with action name in TLA+ for example)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void commitChanges(String eventName) throws IOException {
        commitChanges(eventName, "");
    }

    /**
     * Commit all variable changes in batch
     * @param eventName Name of the event that is committed (may correspond to action name in TLA+ for example)
     * @param args Arguments of the event that is committed (may correspond to action arguments in TLA+ for example)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void commitChanges(String eventName, Object[] args) throws IOException {
        commitChanges(eventName, args, "");
    }

    /**
     * Commit all variable changes in batch
     * @param eventName Name of the event that is committed (may correspond to action name in TLA+ for example)
     * @param desc Description of the commit (custom message)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public void commitChanges(String eventName, String desc) throws IOException {
        commitChanges(eventName, new Object[] {}, desc);
    }

    // Note: I found missing synchronized bug thanks to trace validation
    /**
     * Commit all variable changes in batch
     * @param eventName Name of the event that is committed (may correspond to action name in TLA+ for example)
     * @param args Arguments of the event that is committed (may correspond to action arguments in TLA+ for example)
     * @param desc Description of the commit (custom message)
     * @throws IOException Thrown when unable to write event in trace file
     */
    public synchronized void commitChanges(String eventName, Object[] args, String desc) throws IOException {
        // All events are committed at the same logical time (sync)
        // Sync clock
        this.clock = this.globalClock.sync(this.clock);
        // Commit all previously changed variables
        commitChanges(eventName, desc, args, this.clock);
    }


    /**
     * Token to know if a transaction for a commit is open (experimental)
     */
    private boolean isTransactionOpen;

    /**
     * Experimental: Begin commit changes transaction and sync clock at the same time
     * The value of the clock will be used when we close our transaction (endCommit)
     * This method is useful to keep chronological order when logging some code like network message:
     * If we use commitChanges AFTER the message send, it is possible that another process log something before
     * that commitChanges is applied. It led to have a trace that log action of the process that receive message BEFORE the message is sent.
     * Another way to deal with that is to call commitChanges BEFORE the message send but if the message send fail
     * The trace remains correct at this point when it shouldn't be. However... in long term it's not really a problem because
     * generally it will lead to an incorrect trace, that's why it's an experimental feature.
     * @throws IOException
     */
    public synchronized void beginCommit() throws IOException {
        if (isTransactionOpen)
            throw new IOException("A transaction is already open.");

        this.clock = this.globalClock.sync(this.clock);
        isTransactionOpen = true;
    }

    /**
     * Experimental: Commit the commit changes transaction and use the latest sync clock
     * @param eventName
     * @throws IOException
     */
    public void endCommit(String eventName) throws IOException {
        endCommit(eventName, new Object[]{}, "");
    }

    public void endCommit(String eventName, Object[] args, String desc) throws IOException {
        if (!isTransactionOpen)
            throw new IOException("No transactions have been opened.");

        // Commit all previously changed variables
        commitChanges(eventName, desc, args, this.clock);

        isTransactionOpen = false;
    }

    /**
     * Commit all variable changes in batch
     * @param eventName Name of the event that is committed (may correspond to action name in TLA+ for example)
     * @param desc Description of the commit (custom message)
     * @param args Arguments of the event that is committed (may correspond to action arguments in TLA+ for example)
     * @param clock Instrumentation current clock value
     * @throws IOException Thrown when unable to write event in trace file
     */
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
