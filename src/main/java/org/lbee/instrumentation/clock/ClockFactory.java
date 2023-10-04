package org.lbee.instrumentation.clock;

import java.io.IOException;

import main.java.org.lbee.instrumentation.clock.ClockException;

public class ClockFactory {
    public final static int LOGICAL = 1;
    public final static int LOCAL = 2;

    public static InstrumentationClock getClock(int type, String... name) throws ClockException{
        switch (type) {
            case LOGICAL:
                return new LogicalClock();
            case LOCAL:
                String cn = name.length == 1 ? name[0] : "default";
                try {
                    return new SharedClock(cn);
                } catch (IOException exc) {
                    throw new ClockException("Can't create clock: " + exc.getMessage());
                }
            default:
                return new LogicalClock();
        }
    }
}
