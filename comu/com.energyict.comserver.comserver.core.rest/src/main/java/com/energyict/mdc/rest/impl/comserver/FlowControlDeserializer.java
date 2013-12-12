package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.FlowControl;
import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class FlowControlDeserializer extends JsonDeserializer<FlowControl> {

    @Override
    public FlowControl deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String text = jp.getText();
        switch (text) {
            case "None": return FlowControl.NONE;
            case "Rts/Cts": return FlowControl.RTSCTS;
            case "Dtr/Dsr": return FlowControl.DTRDSR;
            case "Xon/Xoff": return FlowControl.XONXOFF;
            default: return null;
        }
    }
}
