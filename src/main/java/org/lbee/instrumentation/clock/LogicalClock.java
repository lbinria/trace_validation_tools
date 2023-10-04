package org.lbee.instrumentation.clock;

class LogicalClock implements InstrumentationClock {
    // Current value of logical clock
    private long value;

    public LogicalClock() {
        this.value = 0;
    }

    @Override
    public synchronized long getNextTime(long clock) {
        this.value = Math.max(value, clock) + 1;
        return this.value;
    }
}
