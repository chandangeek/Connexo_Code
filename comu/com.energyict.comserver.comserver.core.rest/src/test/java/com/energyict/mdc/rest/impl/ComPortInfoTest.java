package com.energyict.mdc.rest.impl;

import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.rest.impl.comserver.ComPortInfo;
import com.energyict.mdc.rest.impl.comserver.InboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.ModemInboundComPortInfo;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComPortInfoTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
        AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.setDeserializationConfig(objectMapper.getDeserializationConfig().with(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        objectMapper.setAnnotationIntrospector(pair);
    }

    @Test
    public void testComPortInfoSerialization() throws Exception {
        ModemInboundComPortInfo comPort = new ModemInboundComPortInfo();
        comPort.name="new";
        comPort.flowControl= FlowControl.XONXOFF;
        String response = objectMapper.writeValueAsString(comPort);
        assertThat(response).contains("\"name\":\"new\"");
        assertThat(response).contains("\"flowControl\":\"Xon/Xoff\"");
    }

    @Test
    public void testComPortInfoDeserialization() throws Exception {
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

    @Test(expected = JsonMappingException.class)
    public void testComPortInfoDeserializationWithIllegalValue() throws Exception {
        String json = "{" +
                "    'comPortType': 'SERIAL'," +
                "    'id': 201," +
                "    'direction': 'inbound'," +
                "    'name': 'comport name'," +
                "    'flowControl': 'fyk'" +
                "}";
        json = json.replace("'","\"");
        ComPortInfo response = objectMapper.readValue(json, InboundComPortInfo.class);

    }
}
