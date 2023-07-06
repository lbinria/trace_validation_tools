package org.lbee.instrumentation.clock;

public class ClockFactory {
    public static InstrumentationClock getClock(){
        return new LogicalClock();
    }

    private static class LogicalClock implements InstrumentationClock {
        // Current value of logical clock
        private long value;
    
        public LogicalClock() {
            this.value = 0;
        }
    
    
        public synchronized long sync(long clock) {
            this.value = Math.max(value, clock) + 1;
            return this.value;
        }
    }
}
