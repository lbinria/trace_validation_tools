package org.lbee.instrumentation.clock;

import java.util.concurrent.TimeUnit;

public class ClockFactory {

    public static InstrumentationClock getClock(boolean isSystem){
        return new LocalClockInternal();
    }

    public static InstrumentationClock getClock(){
        return new LocalClockInternal();
    }

    static class LocalClockInternal implements InstrumentationClock {

        // Current value of logical clock
        private long value;

        public LocalClockInternal() {
            this.value = 0;
        }

        // Return value
        public synchronized long sync(long clock) {
            this.value = Math.max(this.getValue(), clock) + 1;
            return this.value;
        }

        /**
         * Get elapsed time of clock between now and the moment it was created
         * @return Elapsed time in ms
         */
        // TODO remove getValue
        public long getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return Long.toString(this.value);
        }
    }

    static class SystemClockInternal implements InstrumentationClock {

        private long start;

        private SystemClockInternal() {
            this.start = System.currentTimeMillis();
        }

        @Override
        public synchronized long sync(long clock) {
            // Sync global clock
            return getValue();
        }

        @Override
        public long getValue() {
            return System.currentTimeMillis() - start;
        }
    }
}
