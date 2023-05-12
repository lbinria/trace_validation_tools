package org.lbee.instrumentation;

import java.util.ArrayList;

public final class VirtualField {

    private final String name;
    private final VirtualField parentField;
    private final TraceInstrumentation traceInstrumentation;

    public VirtualField(String name, VirtualField parentField) {
        this.name = name;
        this.parentField = parentField;
        this.traceInstrumentation = parentField.traceInstrumentation;
    }

    public VirtualField(String name, TraceInstrumentation traceInstrumentation) {
        this.name = name;
        this.parentField = null;
        this.traceInstrumentation = traceInstrumentation;
    }

    public VirtualField getField(String name) {
        return new VirtualField(name, this);
    }

    public void set(Object val) {
        apply("Replace", val);
    }

    public void add(Object val) {
        apply("AddElement", val);
    }

    public void remove(Object val) {
        apply("RemoveElement", val);
    }

    public void clear() {
        apply("Clear");
    }

    public void init() {
        apply("Init");
    }

    public void init(Object val) {
        apply("InitWithValue", val);
    }

    public void removeKey(Object key) {
        apply("RemoveKey", key);
    }

    public void apply(String op, Object... args) {
        traceInstrumentation.notifyChange(new VirtualUpdate(this, op, args));
    }

    public ArrayList<String> getPath() {
        final ArrayList<String> path;

        if (parentField == null)
             path = new ArrayList<>();
        else
            path = parentField.getPath();

        path.add(name);
        return path;
    }

    @Override
    public String toString() {
        return "VirtualField{" +
                "name='" + name + '\'' +
                ", parentField=" + parentField +
                ", traceInstrumentation=" + traceInstrumentation +
                '}';
    }
}
