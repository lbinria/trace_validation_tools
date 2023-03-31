package org.lbee.instrumentation;

import org.lbee.instrumentation.clock.ClockFactory;
import org.lbee.instrumentation.clock.InstrumentationClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TraceInstrumentation {

    // Local clock
    private final InstrumentationClock clock;
    // Instrumented values
    private final HashMap<String, TrackedVariable> instrumentedValues;
    // Trace producer
    private final TraceProducer traceProducer;

    private final ArrayList<TrackedVariable> orderedInstrumentedValues;

    public InstrumentationClock getClock() {
        return this.clock;
    }
    // Instrumentation guid
    public String getGuid() { return this.traceProducer.getGuid(); }

    public void sync(long clock) {
        this.clock.sync(clock);
    }

    public TraceInstrumentation(TraceProducer traceProducer, boolean systemClock) {
        this.instrumentedValues = new HashMap<>();
        this.orderedInstrumentedValues = new ArrayList<>();
        this.clock = ClockFactory.getClock(systemClock);
        this.traceProducer = traceProducer;
    }

    public TrackedVariable add(String name, Object value) {
        return add(name, value, new Object[] {});
    }

    public TrackedVariable add(String name, Object value, Object... contextArgs) {
        final TrackedVariable trackedVariable = new TrackedVariable(name, value, this.traceProducer, contextArgs);
        this.instrumentedValues.put(name, trackedVariable);
        this.orderedInstrumentedValues.add(trackedVariable);
        return trackedVariable;
    }

    /**
     * Get a tracked variable by name
     * @param name Tracked variable name
     * @return A tracked variable
     */
    public TrackedVariable get(String name) {
        return this.instrumentedValues.get(name);
    }

    /**
     * Sync log to an action
     * @param action
     */
    public void syncCommit(Runnable action) {
        action.run();
        this.commit();
    }

    /**
     * Commit logs
     */
    public void commit() {
        // All events are committed at the same logical time (sync)
        final long clock = this.clock.getValue();
        // Commit all previously produced traces
        this.traceProducer.commit(clock);
        // Resync clock
        this.clock.sync(clock);
    }

    public void commitChanges() throws TraceProducerException {
        // All events are committed at the same logical time (sync)
        final long clock = this.clock.getValue();
        // Commit all previously changed variables
        this.traceProducer.commitChanges(clock);
        // Resync clock
        this.clock.sync(clock);
    }

}
