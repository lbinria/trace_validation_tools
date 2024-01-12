package org.lbee.instrumentation.clock;

/**
 * A memory clock that can be shared through multiple threads of the same
 * process.
 */
class MemoryClock implements InstrumentationClock {
    // Current value of logical clock
    private long value;

    public MemoryClock() {
        this.value = 0;
    }

    @Override
    public synchronized long getNextTime(long clock) {
        this.value = Math.max(value, clock) + 1;
        return this.value;
    }
}
