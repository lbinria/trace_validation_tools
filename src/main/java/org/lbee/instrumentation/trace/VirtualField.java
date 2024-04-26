package org.lbee.instrumentation.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class VirtualField {

    private final String name;
    private final VirtualField parentField;
    private final TLATracer tracer;
    // private final String var;
    // private final List<String> path;

    public VirtualField(String name, VirtualField parentField) {
        this.name = name;
        this.parentField = parentField;
        this.tracer = parentField.tracer;
    }

    public VirtualField(String name, TLATracer tracer) {
        this.name = name;
        this.parentField = null;
        this.tracer = tracer;
    }

    private String getVar() {
        if (parentField == null) {
            return name;
        }
        return parentField.getVar();
    }

    private List<String> getFullPath() {
        List<String> path = new ArrayList<>();
        if (parentField != null) {
            path = parentField.getFullPath();
        } 
        path.add(name);
        return path;
    }

    private List<String> getPath() {
        List<String> fullPath = this.getFullPath();
        List<String> path = fullPath.subList(1, fullPath.size());
        return path;
    }

    public VirtualField getField(String name) {
        return new VirtualField(name, this);
    }

    public void init() {
        apply("Init");
    }

    public void update(Object val) {
        apply("Update", val);
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
        tracer.notifyChange(this.getVar(), this.getPath(), op, List.of(args));
    }

    @Override
    public String toString() {
        return "VirtualField{" +
                "name='" + name + '\'' +
                ", parentField=" + parentField +
                ", traceInstrumentation=" + tracer +
                '}';
    }
}
