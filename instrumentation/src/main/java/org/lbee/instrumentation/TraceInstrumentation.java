package org.lbee.instrumentation;

import org.lbee.instrumentation.clock.ClockFactory;
import org.lbee.instrumentation.clock.InstrumentationClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TraceInstrumentation {

    // Local clock
    private final InstrumentationClock clock;
    // Trace producer
    private final TraceProducer traceProducer;
    // Instrumented values
    private final ArrayList<TrackedVariable<?>> variables;

    public InstrumentationClock getClock() {
        return this.clock;
    }
    // Instrumentation guid
    public String getGuid() { return this.traceProducer.getGuid(); }

    public void sync(long clock) {
        this.clock.sync(clock);
    }

    public TraceInstrumentation(TraceProducer traceProducer, boolean systemClock) {
        this.variables = new ArrayList<>();
        this.clock = ClockFactory.getClock(systemClock);
        this.traceProducer = traceProducer;
    }

    public <TValue> TrackedVariable<TValue> add(String name, TValue value) {
        return add(name, value, new Object[] {});
    }

    public <TValue> TrackedVariable<TValue> add(String name, TValue value, Object... contextArgs) {
        final TrackedVariable<TValue> trackedVariable = new TrackedVariable<>(name, value, this.traceProducer, contextArgs);
        this.variables.add(trackedVariable);
        return trackedVariable;
    }

    public void notifyChange(String variableName, String action, String[] path, Object... args) {
        this.traceProducer.addUpdate(variableName, action, path, args);
    }

//    /**
//     * Get a tracked variable by name
//     * @param name Tracked variable name
//     * @return A tracked variable
//     */
//    public TrackedVariable<?> get(String name) {
//        return this.variables.get(name);
//    }

    public synchronized void commitChanges() throws TraceProducerException {
        commitChanges(null);
    }

    // Note: I found missing synchronized bug thanks to trace validation
    public synchronized void commitChanges(String description) throws TraceProducerException {
        // All events are committed at the same logical time (sync)
        // Sync clock
        final long clock = this.clock.sync(this.clock.getValue());
        // Commit all previously changed variables
        this.traceProducer.commitChanges(description, clock);
    }

}
