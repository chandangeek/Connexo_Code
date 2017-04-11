package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Custom JSON deserializer for ScheduleableItem class, to be able to handle AbstractDataType of bufferSize
 */
public class ScheduleableItemDataSerializer extends JsonSerializer<SchedulableItem> {

    @Override
    public void serialize(SchedulableItem value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("obisCode", value.getObisCode().toString());
        jgen.writeStringField("bufferSize", ProtocolTools.getHexStringFromBytes(value.getBufferSize().getBEREncodedByteArray(), ""));
        jgen.writeEndObject();
    }
}
