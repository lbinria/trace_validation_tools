package org.lbee.instrumentation.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class VirtualField {

    private final String name;
    private final VirtualField parentField;
    private final TLATracer behaviorRecorder;
    private final String var;
    private final List<String> path;

    public VirtualField(String name, VirtualField parentField) {
        this.name = name;
        this.parentField = parentField;
        this.behaviorRecorder = parentField.behaviorRecorder;
        List<String> fullPath = this.getPath();
        this.var = fullPath.get(0);
        this.path = fullPath.subList(1, fullPath.size());
    }

    public VirtualField(String name, TLATracer behaviorRecorder) {
        this.name = name;
        this.parentField = null;
        this.behaviorRecorder = behaviorRecorder;
        List<String> fullPath = this.getPath();
        this.var = fullPath.get(0);
        this.path = fullPath.subList(1, fullPath.size());
    }

    public VirtualField getField(String name) {
        return new VirtualField(name, this);
    }

    public void init() {
        apply("Init");
    }

    public void set(Object val) {
        apply("Set", val);
    }

    public void add(Object val) {
        apply("AddElement", val);
    }

    public void addAll(Collection<?> vals) {
        apply("AddElements", vals);
    }

    public void remove(Object val) {
        apply("RemoveElement", val);
    }

    public void clear() {
        apply("Clear");
    }

    public void addToBag(Object val) {
        apply("AddElementToBag", val);
    }

    public void removeFromBag(Object val) {
        apply("RemoveElementFromBag", val);
    }

    public void clearBag() {
        apply("ClearBag");
    }

    public void append(Object val) {
        apply("AppendElement", val);
    }

    public void resetKey(Object key) {
        apply("ResetKey", key);
    }

    public void setKey(Object key, Object value) {
        apply("SetKey", key, value);
    }

    public void updateRecord(Object val) {
        apply("UpdateRec", val);
    }

    public void initRecord() {
        apply("InitRec");
    }

    public void apply(String op, Object... args) {
        behaviorRecorder.notifyChange(this.var, op, this.path, List.of(args));
    }

    private List<String> getPath() {
        final List<String> path;

        if (parentField == null) {
            path = new ArrayList<>();
        } else {
            path = parentField.getPath();
        }

        path.add(name);
        return path;
    }

    @Override
    public String toString() {
        return "VirtualField{" +
                "name='" + name + '\'' +
                ", parentField=" + parentField +
                ", traceInstrumentation=" + behaviorRecorder +
                '}';
    }
}
