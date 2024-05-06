package org.lbee.instrumentation.helper;

import com.google.gson.JsonElement;

/**
 * An interface for objects that can be serialized.
 */
public interface TLASerializer {

    JsonElement tlaSerialize();

}
