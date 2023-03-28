package org.lbee.instrumentation.clock;

public class ClockFactory {

    public static InstrumentationClock getClock(boolean isLogical){
        return isLogical ? new LogicalClockInternal() : new RealTimeClockInternal();
    }

    static class LogicalClockInternal implements InstrumentationClock {

        // Current value of logical clock
        private long value;

        public LogicalClockInternal() {
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

    static class RealTimeClockInternal implements InstrumentationClock {

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
