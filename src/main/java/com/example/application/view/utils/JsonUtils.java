package com.example.application.view.utils;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;

public class JsonUtils {
    private JsonUtils() {
        // Static methods only
    }

    public static JsonArray toJsonArray(Iterable<?> list) {
        JsonArray arr = Json.createArray();
        int i = 0;
        for (var o : list) {
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
            arr.set(i++, v);
        }
        return arr;
    }
}
