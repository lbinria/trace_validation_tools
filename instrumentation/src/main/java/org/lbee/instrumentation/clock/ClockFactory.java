package org.lbee.instrumentation.clock;

import java.util.concurrent.TimeUnit;

public class ClockFactory {
    public static InstrumentationClock getClock(){
        return new LogicalClock();
    }
}
