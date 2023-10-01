package org.lbee.instrumentation.clock;

public interface InstrumentationClock {

    long sync(long clock);

    default long sync() {
        return 0L;
    }
}
