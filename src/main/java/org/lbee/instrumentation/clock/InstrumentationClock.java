package org.lbee.instrumentation.clock;

public interface InstrumentationClock {

    long getNextTime(long clock);

    default long getNextTime() {
        return 0L;
    }
}
