package org.example.protocol;

import com.google.gson.*;

/**
 * Represents a protocol request from a client.
 * All protocol messages follow the format: {"command": "NAME", "information": {...}}
 */
public class ProtocolRequest {
    private String command;
    private JsonObject information;

    public ProtocolRequest(String command, JsonObject information) {
        this.command = command;
        this.information = information;
    }

    public String getCommand() {
        return command;
    }

    public JsonObject getInformation() {
        return information;
    }

    /**
     * Parses a JSON string into a ProtocolRequest.
     *
     * @param jsonString the JSON string to parse
     * @return a ProtocolRequest instance
     * @throws JsonSyntaxException if the JSON is invalid
     */
    public static ProtocolRequest parse(String jsonString) throws JsonSyntaxException {
        JsonElement element = JsonParser.parseString(jsonString);
        JsonObject json = element.getAsJsonObject();

        String command = json.get("command").getAsString();
        JsonObject information = json.getAsJsonObject("information");

        return new ProtocolRequest(command, information);
    }

    @Override
    public String toString() {
        return "ProtocolRequest{" +
                "command='" + command + '\'' +
                ", information=" + information +
                '}';
    }
}

