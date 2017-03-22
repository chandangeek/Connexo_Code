package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * * Custom JSON serializer for ScheduleableItem class, to be able to handle AbstractDataType of bufferSize
 */
public class SchedulableItemDeserializer extends JsonDeserializer<SchedulableItem> {
    @Override
    public SchedulableItem deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        String axdrEncodedBufferSize = node.get("bufferSize").getTextValue();
        AbstractDataType bufferSize = AXDRDecoder.decode(ProtocolTools.getBytesFromHexString(axdrEncodedBufferSize,2));
        ObisCode obisCode = ObisCode.fromString(node.get("obisCode").getTextValue());
        return new SchedulableItem(obisCode, bufferSize);
    }
}
