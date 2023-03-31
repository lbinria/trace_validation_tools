package org.lbee.instrumentation;

import com.google.gson.JsonElement;

public interface TraceProducer/*<TSerialized>*/ {

    String getGuid();
    void produce(String op, String name, Object[] args) throws TraceProducerException;
    void trace(TrackedVariable/*<TSerialized>*/ variable, long clock);
    void change(TrackedVariable/*<TSerialized>*/ trackedVariable);
    void commit(long clock);
    void commitChanges(long clock);
    //TSerialized serializeValues(Object... values);
    //TSerialized serializeValue(Object value);
}
