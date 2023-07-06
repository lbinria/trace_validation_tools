package org.lbee.instrumentation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.Map;

public class ConfigurationWriter {

    public static void write(String path, Map<String, Object> configurationMap) throws IOException {
        final JsonObject jsonObject = NDJsonSerializer.jsonObjectOfMap(configurationMap);
        final BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(jsonObject.toString() + "\n");
        writer.close();
    }

    public static JsonObject read(String path) throws IOException {
        final Gson gson = new Gson();
        final BufferedReader reader = new BufferedReader(new FileReader(path));

        String line = reader.readLine();
        if (line == null)
            throw new IOException("Configuration file must contains one json object. Invalid configuration file.");

        final JsonElement jsonLine = JsonParser.parseString(line);
        if (!jsonLine.isJsonObject())
            throw new IOException("Configuration file must contains one json object. Invalid configuration file.");

        return jsonLine.getAsJsonObject();
    }

}
