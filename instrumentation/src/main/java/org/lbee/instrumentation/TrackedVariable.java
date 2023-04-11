package org.lbee.instrumentation;

import java.util.Arrays;
import java.util.stream.Stream;

public class TrackedVariable<TValue> {

    private final String name;
    private final TraceProducer traceProducer;
    private final Object[] contextArgs;
    private TValue value;

    TrackedVariable(String name, TValue value, TraceProducer traceProducer) {
        this(name, value, traceProducer, new Object[] {});
    }

    TrackedVariable(String name, TValue value, TraceProducer traceProducer, Object... contextArgs) {
        this.name = name;
        this.value = value;
        this.traceProducer = traceProducer;
        this.contextArgs = contextArgs;
    }

    public void apply(String operator, Object... args) throws TraceProducerException {
        final Object[] allArgs = Stream.concat(Arrays.stream(contextArgs), Arrays.stream(args)).toArray(Object[]::new);
        this.traceProducer.produce(operator, this.name, allArgs);
    }

    public void notifyChange(TValue value) {
        // Set new value
        this.value = value;
        // Add to changed variables set
        this.traceProducer.notifyChange(this);
    }

    public String getName() { return name; }

    public Object[] getContextArgs() { return contextArgs; }

    public Object getValue() { return value; }

}
