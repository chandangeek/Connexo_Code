package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * * Custom JSON serializer for ScheduleableItem class, to be able to handle AbstractDataType of bufferSize
 */
public class SchedulableItemDeserializer extends JsonDeserializer<SchedulableItem> {
    @Override
    public SchedulableItem deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        String axdrEncodedBufferSize = node.get("bufferSize").textValue();
        AbstractDataType bufferSize = AXDRDecoder.decode(ProtocolTools.getBytesFromHexString(axdrEncodedBufferSize, 2));
        ObisCode obisCode = ObisCode.fromString(node.get("obisCode").textValue());
        return new SchedulableItem(obisCode, bufferSize);
    }
}