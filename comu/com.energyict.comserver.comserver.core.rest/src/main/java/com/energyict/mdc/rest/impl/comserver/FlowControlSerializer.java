package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.channels.serial.FlowControl;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class FlowControlSerializer extends JsonSerializer<FlowControl> {

    @Override
    public void serialize(FlowControl value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (value!=null) {
            switch (value) {
                case NONE:
                    jgen.writeString("None");
                    break;
                case RTSCTS:
                    jgen.writeString("Rts/Cts");
                    break;
                case DTRDSR:
                    jgen.writeString("Dtr/Dsr");
                    break;
                case XONXOFF:
                    jgen.writeString("Xon/Xoff");
                    break;
            }
        }
    }
}
