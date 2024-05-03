package org.lbee.instrumentation.trace;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.lbee.instrumentation.clock.ClockFactory;
import org.lbee.instrumentation.clock.ClockException;
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
    // unique id
    private final String guid;
    // clock providing the next time value
    private final InstrumentationClock clock;
    // writer used to write the trace
    private final BufferedWriter writer;
    // for each logged variable store the modifications made since the last log
    private final HashMap<String, List<TraceItem>> updates;

    /**
     * Create a new tracer.
     * 
     * @param writer The writer used to write the trace.
     * @param clock  The clock used when logging.
     */
    private TLATracer(BufferedWriter writer, InstrumentationClock clock) {
        this.clock = clock;
        this.writer = writer;
        this.guid = UUID.randomUUID().toString();
        this.updates = new HashMap<>();
    }

    /**
     * Create a new tracer with a specified clock.
     * 
     * @param tracePath The path of the trace file.
     * @param clock     The clock used when logging.
     * @return A new instrumentation.
     * @throws IOException Thrown when unable to create trace file.
     */
    public static TLATracer getTracer(String tracePath, InstrumentationClock clock) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(tracePath));
        return new TLATracer(writer, clock);
    }

    /**
     * Create a new instrumentation for the case where the clock is handled locally
     * by each process.
     * 
     * @param tracePath The path of the trace file.
     * @return A new instrumentation.
     * @throws IOException    Thrown when unable to create trace file.
     * @throws ClockException Thrown when unable to create clock (normally never
     *                        thrown for a LOCAL clock).
     */
    public static TLATracer getTracer(String tracePath) throws IOException, ClockException {
        return getTracer(tracePath, ClockFactory.getClock(ClockFactory.LOCAL));
    }

    /**
     * Notify the modification of the value of a variable. The action performed on
     * the variable (using an operator) is added to the list of actions performed
     * since the last log.
     * 
     * @param variable Name of the variable that has been modified.
     * @param operator Operator applied to the variable to change its value.
     *                 This should correspond to one of the operators defined in
     *                 TVOperators.tla.
     * @param path     Path for the field of the variable which is modified (for
     *                 example, ['address','city'] if the variable represents a
     *                 person having a field address (and possibly other fields),
     *                 and the address has a field city (and possibly other fields).
     * @param args     Arguments used by the operator.
     */
    public synchronized void notifyChange(String variable, List<Object> path, String operator, List<Object> args) {
        // check if a modification has been already notified for the variable
        if (!updates.containsKey(variable)) {
            updates.put(variable, new ArrayList<>());
        }
        // add the action to the list of actions
        updates.get(variable).add(new TraceItem(operator, path, args));
    }

    /**
     * Commit an exception caught in the implementation.
     * 
     * @param desc Description of the exception.
     * @throws IOException When unable to write event in trace file
     */
    public void logException(String desc) throws IOException {
        this.log("__exception", desc);
    }

    /**
     * Get a virtual variable, that is used to notify later changes of the
     * corresponding concrete variable.
     * 
     * @param variableName Name of the variable
     * @return A virtual variable on which changes can be notified
     */
    public VirtualField getVariableTracer(String variableName) {
        return new VirtualField(variableName, this);
    }

    /**
     * Commit all variable changes in batch. Used by the log methods to write the
     * changes to the trace file.
     * 
     * @param eventName  Name of the event corresponding to the commit (generally
     *                   corresponds to an action name in the TLA+ specification).
     * @param args       Arguments of the event that is committed (generally
     *                   correspond to action arguments in the TLA+ specification).
     * @param desc       Description of the commit (custom message).
     * @param clockValue Clock value at the time the commit is requested.
     * @throws IOException Thrown when unable to write event in trace file.
     */
    private synchronized void logChanges(String eventName, Object[] args, String desc, long clockValue)
            throws IOException {
        final JsonObject jsonEvent = new JsonObject();
        // Set clock
        jsonEvent.addProperty("clock", clockValue);
        try {
            // add actions for each of the updated variables
            for (String variableName : this.updates.keySet()) {
                // all actions made on the updated variable
                final List<TraceItem> actions = new ArrayList<>(this.updates.get(variableName));
                final JsonArray jsonActions = new JsonArray();
                for (TraceItem action : actions) {
                    jsonActions.add(action.jsonize());
                }
                jsonEvent.add(variableName, jsonActions);
            }

            // set eventName and arguments if provided
            if (eventName != null && !eventName.equals("")) {
                jsonEvent.addProperty("event", eventName);
            }
            if (args != null && args.length > 0) {
                jsonEvent.add("event_args", NDJsonSerializer.jsonArrayOf(args));
            }
            // set desc if provided
            if (desc != null && !desc.equals("")) {
                jsonEvent.addProperty("desc", desc);
            }
        } catch (IllegalAccessException e) {
            // set event to exception, fill description with exception message
            eventName = "__exception";
            desc = e.toString();
        }
        // set the id of the logger
        jsonEvent.addProperty("logger", this.guid);
        // record to file
        writer.write(jsonEvent + "\n");
        writer.flush();
        // clear for next log
        this.updates.clear();
    }

    /**
     * Commit all variable changes since the previous log or since the creation of
     * the tracer.
     * 
     * @param eventName  Name of the event corresponding to the commit (generally
     *                   corresponds to an action name in the TLA+ specification).
     * @param args       Arguments of the event that is committed (generally
     *                   correspond to action arguments in the TLA+ specification).
     * @param desc       Description of the commit (custom message).
     * @param clockValue Clock value at the time the commit is requested.
     * @return the clock value used to log the event.
     * @throws IOException Thrown when unable to write event in trace file.
     */
    public long log(String eventName, Object[] args, long clockValue, String desc) throws IOException {
        // Update global clock et get the next clock value
        long newClockValue = this.clock.getNextTime(clockValue);
        // Commit all previously changed variables
        this.logChanges(eventName, args, desc, newClockValue);
        return newClockValue;
    }

    /**
     * Commit all variable changes since the previous log or since the creation of
     * the tracer.
     * 
     * @param eventName  Name of the event corresponding to the commit (generally
     *                   corresponds to an action name in the TLA+ specification).
     * @param args       Arguments of the event that is committed (generally
     *                   correspond to action arguments in the TLA+ specification).
     * @param desc       Description of the commit (custom message).
     * @return the clock value used to log the event.
     * @throws IOException Thrown when unable to write event in trace file.
     */
    public long log(String eventName, Object[] args, String desc) throws IOException {
        return this.log(eventName, args, 0L, desc);
    }

    /**
     * Commit all variable changes since the previous log or since the creation of
     * the tracer.
     * 
     * @param eventName  Name of the event corresponding to the commit (generally
     *                   corresponds to an action name in the TLA+ specification).
     * @param desc       Description of the commit (custom message).
     * @return the clock value used to log the event.
     * @throws IOException Thrown when unable to write event in trace file.
     */
    public long log(String eventName, String desc) throws IOException {
        return this.log(eventName, new Object[] {}, desc);
    }

    /**
     * Commit all variable changes since the previous log or since the creation of
     * the tracer.
     * 
     * @param eventName  Name of the event corresponding to the commit (generally
     *                   corresponds to an action name in the TLA+ specification).
     * @param args       Arguments of the event that is committed (generally
     *                   correspond to action arguments in the TLA+ specification).
     * @return the clock value used to log the event.
     * @throws IOException Thrown when unable to write event in trace file.
     */
    public long log(String eventName, Object[] args) throws IOException {
        return this.log(eventName, args, "");
    }

    /**
     * Commit all variable changes since the previous log or since the creation of
     * the tracer.
     * 
     * @param eventName  Name of the event corresponding to the commit (generally
     *                   corresponds to an action name in the TLA+ specification).
     * @return the clock value used to log the event.
     * @throws IOException Thrown when unable to write event in trace file.
     */
    public long log(String eventName) throws IOException {
        return this.log(eventName, "");
    }

    /**
     * Commit all variable changes since the previous log or since the creation of
     * the tracer.
     * 
     * @return the clock value used to log the event.
     * @throws IOException Thrown when unable to write event in trace file.
     */
    public long log() throws IOException {
        return this.log("");
    }
}
