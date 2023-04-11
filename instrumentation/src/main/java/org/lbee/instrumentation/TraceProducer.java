package org.lbee.instrumentation;

public interface TraceProducer {

    String getGuid();
    void produce(String op, String name, Object[] args) throws TraceProducerException;
    void trace(TrackedVariable<?> variable, long clock);
    void addUpdate(String variableName, String action, String[] path, Object[] args);
    void notifyChange(TrackedVariable<?> trackedVariable);
    void commit(long clock);
    void commitChanges(long clock);

}
