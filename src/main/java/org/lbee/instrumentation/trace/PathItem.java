package org.lbee.instrumentation.trace;

/**
 * Represents a path item which is either a name (of a variable or of a field),
 * or an index (in a sequence) or a boolean.
 */
public record PathItem(String name, Integer index, Boolean bindex) {
}
