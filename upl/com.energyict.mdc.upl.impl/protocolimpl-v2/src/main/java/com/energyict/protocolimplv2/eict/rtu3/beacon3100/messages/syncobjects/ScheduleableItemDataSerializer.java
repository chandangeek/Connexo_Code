package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Custom JSON deserializer for ScheduleableItem class, to be able to handle AbstractDataType of bufferSize
 */
public class ScheduleableItemDataSerializer extends JsonSerializer<SchedulableItem> {

    @Override
    public void serialize(SchedulableItem value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("obisCode", value.getObisCode().toString());
        jgen.writeStringField("bufferSize", ProtocolTools.getHexStringFromBytes(value.getBufferSize().getBEREncodedByteArray(),""));
        jgen.writeEndObject();
    }
}
