package org.lbee.instrumentation;

import org.lbee.instrumentation.clock.ClockFactory;
import org.lbee.instrumentation.clock.InstrumentationClock;
import org.lbee.instrumentation.clock.LogicalClock;
import org.lbee.instrumentation.clock.RealTimeClock;
import org.lbee.instrumentation.config.FormalInstrumentationConfig;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

//public class TraceInstrumentation<TProducer extends TraceProducer> {
public class TraceInstrumentation {

    // Local clock
    private final InstrumentationClock clock;
    // Instrumented values
    private final HashMap<String, TrackedVariable> instrumentedValues;
    // Trace producer
    private final TraceProducer traceProducer;

    public InstrumentationClock getClock() {
        return this.clock;
    }
    // Instrumentation guid
    public String getGuid() { return this.traceProducer.getGuid(); }

    public void sync(long clock) {
        this.clock.sync(clock);
    }

    //    public TraceInstrumentation(TProducer traceProducer, boolean logicalClock) {
    public TraceInstrumentation(TraceProducer traceProducer, boolean logicalClock) {
        this.instrumentedValues = new HashMap<>();
        this.clock = ClockFactory.getClock(logicalClock);
        this.traceProducer = traceProducer;
    }

    public TrackedVariable add(String name) {
        final TrackedVariable trackedVariable = new TrackedVariable(name, this.traceProducer);
        this.instrumentedValues.put(name, trackedVariable);
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

}
