package org.lbee.instrumentation.clock;

import java.io.IOException;

public class ClockFactory {
    public final static int LOCAL = 0;
    public final static int MEMORY = 1;
    public final static int FILE = 2;

    public static InstrumentationClock getClock(int type, String... name) throws ClockException{
        switch (type) {
            case LOCAL:
                return new LocalClock();
            case MEMORY:
                return new MemoryClock();
            case FILE:
                String cn = name.length == 1 ? name[0] : "default";
                try {
                    return new FileClock(cn);
                } catch (IOException exc) {
                    throw new ClockException("Can't create clock: " + exc.getMessage());
                }
            default:
                return new MemoryClock();
        }
    }
}
