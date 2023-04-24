package org.lbee.instrumentation;

public interface TraceProducer {

    String getGuid();
    void produce(String op, String name, Object[] args) throws TraceProducerException;
    void trace(TrackedVariable<?> variable, String description, long clock);
    void addUpdate(String variableName, String action, String[] path, Object[] args);
    void notifyChange(TrackedVariable<?> trackedVariable);
    void commit(String description, long clock);
    void commitChanges(String description, long clock);

}
