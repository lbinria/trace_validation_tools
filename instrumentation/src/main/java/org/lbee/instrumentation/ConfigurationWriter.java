package org.lbee.instrumentation;

import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationWriter {

    public static void write(String path, Map<String, Object> configurationMap) throws IOException {
        final JsonObject jsonObject = NDJsonSerializer.jsonObjectOfMap(configurationMap);
        final BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(jsonObject.toString() + "\n");
        writer.close();
    }

}
