package com.indra.attendance_control.commons;

import java.io.IOException;
import java.time.LocalTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class FlexibleLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    
    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        // Si es un string: "08:00:00"
        if (node.isTextual()) {
            return LocalTime.parse(node.asText());
        }
        
        // Si es un objeto: {hour: 8, minute: 0, second: 0, nano: 0}
        if (node.isObject()) {
            int hour = node.has("hour") ? node.get("hour").asInt() : 0;
            int minute = node.has("minute") ? node.get("minute").asInt() : 0;
            int second = node.has("second") ? node.get("second").asInt() : 0;
            
            return LocalTime.of(hour, minute, second);
        }
        
        throw new IOException("Unable to parse LocalTime from: " + node);
    }
}