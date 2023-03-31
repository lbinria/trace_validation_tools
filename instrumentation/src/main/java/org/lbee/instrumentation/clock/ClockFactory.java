package org.lbee.instrumentation.clock;

public class ClockFactory {

    public static InstrumentationClock getClock(boolean isSystem){
        return isSystem ? new SystemClockInternal() : new LocalClockInternal();
    }

    static class LocalClockInternal implements InstrumentationClock {

        // Current value of logical clock
        private long value;

        public LocalClockInternal() {
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

    static class SystemClockInternal implements InstrumentationClock {

        private final long start;

        private SystemClockInternal() {
            this.start = System.currentTimeMillis();
        }

        @Override
        public void sync(long clock) {
            // Nothing to do
        }

        @Override
        public long getValue() {
            return System.currentTimeMillis();
        }
    }
}
