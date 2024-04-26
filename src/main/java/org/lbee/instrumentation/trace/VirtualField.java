package org.lbee.instrumentation.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class VirtualField {

    private final PathItem item;
    private final VirtualField parentField;
    private final TLATracer tracer;

    public VirtualField(String name, TLATracer tracer) {
        this.item = new PathItem(name, null, null);
        this.parentField = null;
        this.tracer = tracer;
    }

    private VirtualField(String name, Integer index, Boolean bindex, VirtualField parentField) {
        this.item = new PathItem(name, index, bindex);
        this.parentField = parentField;
        this.tracer = parentField.tracer;
    }

    public VirtualField getField(String name) {
        return new VirtualField(name, null, null, this);
    }

    public VirtualField getField(int index) {
        return new VirtualField(null, index, null, this);
    }

    public VirtualField getField(boolean bindex) {
        return new VirtualField(null, null, bindex, this);
    }

    private String getVar() {
        if (parentField == null) {
            return this.item.name();
        }
        return parentField.getVar();
    }

    private List<Object> getFullPath() {
        List<Object> path = new ArrayList<>();
        if (parentField != null) {
            path = parentField.getFullPath();
        } 
        if(item.name() != null) {
            path.add(item.name());
        } else if (item.index() != null) {
            path.add(item.index());
        } else if (item.bindex() != null) {
            path.add(item.bindex());
        } else { // should never happen
            path.add(null);
        }
        return path;
    }

    private List<Object> getPath() {
        List<Object> fullPath = this.getFullPath();
        List<Object> path = fullPath.subList(1, fullPath.size());
        return path;
    }

    public void apply(String op, Object... args) {
        tracer.notifyChange(this.getVar(), this.getPath(), op, List.of(args));
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
}
