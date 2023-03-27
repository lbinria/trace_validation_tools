package org.lbee.instrumentation.clock;

/**
 * Logical clock
 */
public class LogicalClock implements InstrumentationClock {

    // Current value of logical clock
    private long value;

    public LogicalClock() {
        this.value = 0;
    }

    public void sync(long clock) {
        this.value = Math.max(this.getValue(), clock) + 1;
    }

    /**
     * Get elapsed time of clock between now and the moment it was created
     * @return Elapsed time in ms
     */
    public long getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return Long.toString(this.getValue());
    }
}
