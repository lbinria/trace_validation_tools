package org.lbee.instrumentation.clock;

public interface InstrumentationClock {

    void sync(long clock);

    /**
     * Get elapsed time of clock between now and the moment it was created
     * @return Elapsed time in ms
     */
    long getValue();
}
