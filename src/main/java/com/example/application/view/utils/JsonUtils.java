package com.example.application.view.utils;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;

public class JsonUtils {
    private JsonUtils() {
        // Static methods only
    }

    public static JsonValue getJsonValue(Object o) {
        JsonValue v;
        if (o == null) {
            v = Json.createNull();
        } else if (o instanceof Number n) {
            v = Json.create(n.doubleValue());
        } else if (o instanceof Boolean b) {
            v = Json.create(b);
        } else {
            v = Json.create(o.toString());
        }
        return v;
    }

    public static void setJsonValue(JsonArray arr, int index, Object o) {
        arr.set(index, getJsonValue(o));
    }
}
