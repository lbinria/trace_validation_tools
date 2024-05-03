package org.lbee.instrumentation.trace;

import org.lbee.instrumentation.helper.NDJsonSerializer;
import java.util.List;

import com.google.gson.JsonObject;

/**
 * Represents a single trace item w.r.t. a variable. It consists of the path to
 * the field of the variable that is being traced, the action that is being
 * applied to the field and the arguments of the action.
 */
class TraceItem {
    private final List<Object> path;
    private final String action;
    private final List<Object> args;

    public TraceItem(String action, List<Object> path, List<Object> args) {
        this.action = action;
        this.path = path;
        this.args = args;
    }

    /**
     * Converts the trace item to a JSON object.
     * 
     * @return the JSON object representing the trace item
     * @throws IllegalAccessException if the path or the arguments cannot be accessed
     */
    public JsonObject jsonize() throws IllegalAccessException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("op", this.action);
        jsonObject.add("path", NDJsonSerializer.jsonArrayOf(path));
        jsonObject.add("args", NDJsonSerializer.jsonArrayOf(args));
        return jsonObject;
    }
}
