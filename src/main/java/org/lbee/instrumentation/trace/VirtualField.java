package org.lbee.instrumentation.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Used to trace changes to a variable or to a field of a variable usiing a
 * given tracer. A null parentField indicates that we trace a variable at the
 * top level and in this case the item is necessarily a String representing its
 * name. Otherwise, it's either the field of a variable (if the item is a
 * String), or an index in a sequence (if the item is an Integer), or a boolean
 * index (if the item is a Boolean).
 */
public final class VirtualField {
    private final TLATracer tracer;
    private final PathItem item;
    private final VirtualField parentField;

    /**
     * Creates a VirtualField for a variable at the top level.
     * @param name   the name of the variable
     * @param tracer the tracer to use
     */
    public VirtualField(String name, TLATracer tracer) {
        this.item = new PathItem(name, null, null);
        this.parentField = null;
        this.tracer = tracer;
    }

    /**
     * Creates a VirtualField for a field of a variable. Only one of name, index and
     * bindex should be non-null.
     * @param name        the name of the field
     * @param index       the index of the field
     * @param bindex      the boolean index of the field
     * @param parentField the parent field
     */
    private VirtualField(String name, Integer index, Boolean bindex, VirtualField parentField) {
        this.item = new PathItem(name, index, bindex);
        this.parentField = parentField;
        this.tracer = parentField.tracer;
    }

    /**
     * Returns a VirtualField for a field of a variable.
     * @param name
     * @return
     */
    public VirtualField getField(String name) {
        return new VirtualField(name, null, null, this);
    }

    /**
     * Returns a VirtualField for an index in a sequence.
     * @param name
     * @return
     */
    public VirtualField getField(int index) {
        return new VirtualField(null, index, null, this);
    }

    /**
     * Returns a VirtualField for a boolean index.
     * @param bindex
     * @return
     */
    public VirtualField getField(boolean bindex) {
        return new VirtualField(null, null, bindex, this);
    }

    /**
     * Returns the top variable name for the this field.
     * @return the top variable name for the this field
     */
    private String getVar() {
        if (parentField == null) {
            return this.item.name();
        }
        return parentField.getVar();
    }

    /**
     * Returns the full path of this field, including the top variable name.
     * @return the full path of this field
     */
    private List<Object> getFullPath() {
        List<Object> path = new ArrayList<>();
        if (parentField != null) {
            path = parentField.getFullPath();
        }
        if (item.name() != null) {
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

    /**
     * Returns the path of this field, empty if the field is a top variable.
     * @return the path of this field
     */
    private List<Object> getPath() {
        List<Object> fullPath = this.getFullPath();
        List<Object> path = fullPath.subList(1, fullPath.size());
        return path;
    }

    /**
     * Notifies the tracer of a change to this field.
     * @param op    the operation
     * @param args  the arguments of the operation
     */
    public void apply(String op, Object... args) {
        tracer.notifyChange(this.getVar(), this.getPath(), op, List.of(args));
    }

    /**
     * Notifies the tracer that an initialisation is applied to this field.
     */
    public void init() {
        apply("Init");
    }

    /**
     * Notifies the tracer that an update is applied to this field.
     * @param val new value of the field
     */
    public void update(Object val) {
        apply("Update", val);
    }

    /**
     * Notifies the tracer that an object is added to this field.
     * @param val the object to add
     */
    public void add(Object val) {
        apply("AddElement", val);
    }

    /**
     * Notifies the tracer that a collection of objects is added to this field.
     * @param vals the collection of objects to add
     */
    public void addAll(Collection<?> vals) {
        apply("AddElements", vals);
    }

    /**
     * Notifies the tracer that an object is removed from this field.
     * @param val the object to remove
     */
    public void remove(Object val) {
        apply("RemoveElement", val);
    }

    /**
     * Notifies the tracer that the field is cleared.
     */
    public void clear() {
        apply("Clear");
    }

    /**
     * Notifies the tracer that an object is added to a bag.
     * @param val the object to add
     */
    public void addToBag(Object val) {
        apply("AddElementToBag", val);
    }

    /**
     * Notifies the tracer that an object is removed from a bag.
     * @param val the object to remove
     */
    public void removeFromBag(Object val) {
        apply("RemoveElementFromBag", val);
    }

    /**
     * Notifies the tracer that an object is added to a bag.
     * @param val the object to add
     */
    public void addValToBag(Object val) {
        apply("AddToBag", val);
    }

    /**
     * Notifies the tracer that an object is removed from a bag.
     * @param val the object to remove
     */
    public void removeValFromBag(Object val) {
        apply("RemoveFromBag", val);
    }

    /**
     * Notifies the tracer that the bag is cleared.
     */
    public void clearBag() {
        apply("ClearBag");
    }

    /**
     * Notifies the tracer that an object is added to a sequence.
     * @param val the object to add
     */
    public void append(Object val) {
        apply("AppendElement", val);
    }

    /**
     * Notifies the tracer that the value for a key in a map is reset.
     * @param key the key to reset
     */
    public void resetKey(Object key) {
        apply("ResetKey", key);
    }

    /**
     * Notifies the tracer that the value for a key in a map is set.
     * @param key   the key to set
     * @param value the new value 
     */
    public void setKey(Object key, Object value) {
        apply("SetKey", key, value);
    }

    /**
     * Notifies the tracer that a record is updated.
     * @param val the new value of the record
     */
    public void updateRecord(Object val) {
        apply("UpdateRec", val);
    }

    /**
     * Notifies the tracer that a record is initialised.
     */
    public void initRecord() {
        apply("InitRec");
    }
}
