package org.lbee.instrumentation;

import java.util.Arrays;

public final class VirtualUpdate {

    private final VirtualField field;
    private final String op;
    private final Object[] args;

    public VirtualUpdate(VirtualField field, String op, Object[] args) {
        this.field = field;
        this.op = op;
        this.args = args;
    }

    private String[] getCompletePath() {
        // TODO memorize
        return field.getPath().toArray(String[]::new);
    }

    public String[] getPath() {
        return Arrays.stream(getCompletePath()).skip(1).toArray(String[]::new);
    }

    public String getVariableName() {
        return getCompletePath()[0];
    }

    public String getOp() {
        return op;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "VirtualUpdate{" +
                "field=" + field +
                ", op='" + op + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
