package com.energyict.mdc.rest.impl;

import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.rest.impl.comserver.ComPortInfo;
import com.energyict.mdc.rest.impl.comserver.InboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.ModemInboundComPortInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComPortInfoTest {
    @Test
    public void testComPortInfoSerialization() throws Exception {
        ModemInboundComPortInfo comPort = new ModemInboundComPortInfo();
        comPort.name="new";
        comPort.flowControl= FlowControl.XONXOFF;
        ObjectMapper objectMapper = new ObjectMapper();
        String response = objectMapper.writeValueAsString(comPort);
        assertThat(response).contains("\"name\":\"new\"");
        assertThat(response).contains("\"flowControl\":\"Xon/Xoff\"");
    }

    @Test
    public void testComPortInfoDeserialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{" +
                "    'comPortType': 'SERIAL'," +
                "    'id': 201," +
                "    'direction': 'inbound'," +
                "    'name': 'comport name'," +
                "    'flowControl': 'Xon/Xoff'" +
                "}";
        json = json.replace("'","\"");
        ComPortInfo response = objectMapper.readValue(json, InboundComPortInfo.class);
        assertThat(response.flowControl).isEqualTo(FlowControl.XONXOFF);
    }

    @Test
    public void testComPortInfoDeserializationWithEmptyString() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{" +
                "    'comPortType': 'SERIAL'," +
                "    'id': 201," +
                "    'direction': 'inbound'," +
                "    'name': 'comport name'," +
                "    'flowControl': ''" +
                "}";
        json = json.replace("'","\"");
        ComPortInfo response = objectMapper.readValue(json, InboundComPortInfo.class);
        assertThat(response.flowControl).isNull();
    }
}
