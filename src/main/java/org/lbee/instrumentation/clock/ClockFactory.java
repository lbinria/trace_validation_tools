package org.lbee.instrumentation.clock;

import java.io.IOException;

public class ClockFactory {
    public final static int LOCAL = 0;
    public final static int MEMORY = 1;
    public final static int FILE = 2;
    public final static int SERVER = 3;

    public static InstrumentationClock getClock(int type, String... name) throws ClockException {
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
            case SERVER:
                String ip = name.length >= 1 ? name[0] : "localhost";
                int port = name.length == 2 ? Integer.parseInt(name[1]) : 6666;
                return ClientClock.getInstance(ip, port);
            default:
                return new MemoryClock();
        }
    }
}
