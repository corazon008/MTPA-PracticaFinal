package org.example.protocol;

import com.google.gson.*;

/**
 * Represents a protocol response from the server.
 * Response format: {"type": "OK|ERROR", "message": "...", "data": {...}}
 */
public class ProtocolResponse {
    private String type;  // OK or ERROR
    private String message;
    private JsonElement data;

    /**
     * Constructs a ProtocolResponse.
     *
     * @param type the response type ("OK" or "ERROR")
     * @param message the response message
     * @param data optional data object
     */
    public ProtocolResponse(String type, String message, JsonElement data) {
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public ProtocolResponse(String type, String message) {
        this(type, message, null);
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public JsonElement getData() {
        return data;
    }

    /**
     * Converts the response to a JSON string.
     *
     * @return JSON representation of the response
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("message", message);
        if (data != null) {
            json.add("data", data);
        }
        return json.toString();
    }

    /**
     * Creates a success response.
     *
     * @param message the success message
     * @return a ProtocolResponse with type "OK"
     */
    public static ProtocolResponse ok(String message) {
        return new ProtocolResponse("OK", message);
    }

    /**
     * Creates a success response with data.
     *
     * @param message the success message
     * @param data the response data
     * @return a ProtocolResponse with type "OK" and data
     */
    public static ProtocolResponse ok(String message, JsonElement data) {
        return new ProtocolResponse("OK", message, data);
    }

    /**
     * Creates an error response.
     *
     * @param message the error message
     * @return a ProtocolResponse with type "ERROR"
     */
    public static ProtocolResponse error(String message) {
        return new ProtocolResponse("ERROR", message);
    }

    @Override
    public String toString() {
        return toJson();
    }
}

