package org.lbee.instrumentation.clock;

/**
 * A local clock that simply does not modify the local time. It can be used when
 * the proceses logging events handle their own clock synchronization
 */
public class LocalClock implements InstrumentationClock {
    @Override
    public long getNextTime(long clock) {
        return clock;
    }
}
