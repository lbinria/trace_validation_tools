package org.lbee.instrumentation.clock;

public class LogicalClock implements InstrumentationClock {

    // Current value of logical clock
    private long value;

    public LogicalClock() {
        this.value = 0;
    }


    public synchronized long sync(long clock) {
        this.value = Math.max(value, clock) + 1;
        return this.value;
    }

    /**
     * Get elapsed time of clock between now and the moment it was created
     * @return Elapsed time in ms
     */
    public long getValue() {
        return this.value;
    }

    public String toString() {
        return Long.toString(this.value);
    }

}
