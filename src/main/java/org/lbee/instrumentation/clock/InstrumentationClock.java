package org.lbee.instrumentation.clock;

public interface InstrumentationClock {
    /**
     * Get the next time. The clock value depends on the local clock of all
     * processes using it and in particular on the clock of the process asking for
     * the next time.
     * 
     * @param clock current time of the process asking for the next time
     * @return the next time
     */
    long getNextTime(long clock);

    /**
     * If the process asking the next time just wants to rely on the centralized
     * clock for the next value (e.g. when it hadn't changed locally the value of
     * its clock), the parameter is irrelevant.
     * 
     * @return the next time
     */
    default long getNextTime() {
        return getNextTime(0L);
    }
}
