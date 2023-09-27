package org.lbee.instrumentation.trace;

import java.util.List;

import com.google.gson.JsonObject;

class TraceItem {
    private final String action;
    private List<String> path;
    private final List<Object> args;

    public TraceItem(String action, List<String> path, List<Object> args) {
        this.action = action;
        this.path = path;
        this.args = args;
    }

    public JsonObject jsonize() throws IllegalAccessException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("op", this.action);
        jsonObject.add("path", NDJsonSerializer.jsonArrayOf(path));
        jsonObject.add("args", NDJsonSerializer.jsonArrayOf(args));
        return jsonObject;
    }
    
}
