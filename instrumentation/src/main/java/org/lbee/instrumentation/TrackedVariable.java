package org.lbee.instrumentation;

public class TrackedVariable {

    private final String name;
    private final TraceProducer traceProducer;

    public TrackedVariable(String name, TraceProducer traceProducer) {
        this.name = name;
        this.traceProducer = traceProducer;
    }

    public void apply(String operator, Object... args) throws TraceProducerException {
        this.traceProducer.produce(operator, this.name, args);
    }

}
