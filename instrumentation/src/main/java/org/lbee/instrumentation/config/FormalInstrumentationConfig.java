package org.lbee.instrumentation.config;

public class FormalInstrumentationConfig {

    private final boolean logicClock;

    public FormalInstrumentationConfig(boolean logicClock) {
        this.logicClock = logicClock;
    }

    public boolean isLogicClock() {
        return logicClock;
    }


}
