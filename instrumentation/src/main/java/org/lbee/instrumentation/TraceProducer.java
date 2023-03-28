package org.lbee.instrumentation;

public interface TraceProducer {

    String getGuid();
    void produce(String op, String name, Object[] args) throws TraceProducerException;
    void commit(long clock);
}
