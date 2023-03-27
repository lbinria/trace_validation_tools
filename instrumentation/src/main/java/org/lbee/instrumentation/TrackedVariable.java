package org.lbee.instrumentation;

public class TrackedVariable {

    private String name;
    private TraceProducer traceProducer;

    public TrackedVariable(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return this.name; }

    public void setTraceProducer(TraceProducer traceProducer) {
        this.traceProducer = traceProducer;
    }

    public void apply(String operator, Object... args) throws TraceProducerException {
        this.traceProducer.produce(operator, this.name, args, 0);
    }

}
