package org.lbee.instrumentation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class VirtualUpdate {

    private final VirtualField field;
    private final String op;
    private final List<Object> args;

    public VirtualUpdate(VirtualField field, String op, List<Object> args) {
        this.field = field;
        this.op = op;
        this.args = Collections.unmodifiableList(args);
    }

    public List<String> getPrefixPath() {
        return field.getPath().stream().skip(1).collect(Collectors.toList());
    }

    public String getVariableName() {
        return field.getPath().get(0);
    }

    public String getOp() {
        return op;
    }

    public List<Object> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "VirtualUpdate{" +
                "field=" + field +
                ", op='" + op + '\'' +
                ", args=" + args +
                '}';
    }
}
