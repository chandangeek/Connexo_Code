/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl;

import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.rest.impl.comserver.ComPortInfo;
import com.energyict.mdc.rest.impl.comserver.FlowControlInfo;
import com.energyict.mdc.rest.impl.comserver.InboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.ModemInboundComPortInfo;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComPortInfoTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
        AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true);
        objectMapper.setAnnotationIntrospector(pair);
    }

    @Test
    public void testComPortInfoSerialization() throws Exception {
        ModemInboundComPortInfo comPort = new ModemInboundComPortInfo();
        comPort.name="new";
        comPort.flowControl = new FlowControlInfo();
        comPort.flowControl.id= FlowControl.XONXOFF;
        String response = objectMapper.writeValueAsString(comPort);
        assertThat(response).contains("\"name\":\"new\"");
        assertThat(response).contains("\"flowControl\":{\"id\":\"flowcontrol_xon_xoff\"}");
    }

    @Test
    public void testComPortInfoDeserialization() throws Exception {
        String json = "{" +
                "    'type': 'inbound_SERIAL'," +
                "    'comPortType': {'id': 'TYPE_SERIAL'}," +
                "    'id': 201," +
                "    'direction': 'inbound'," +
                "    'name': 'comport name'," +
                "    'flowControl': {'id': 'flowcontrol_xon_xoff'}" +
                "}";
        json = json.replace("'","\"");
        ComPortInfo response = objectMapper.readValue(json, InboundComPortInfo.class);
        assertThat(response.flowControl.id).isEqualTo(FlowControl.XONXOFF);
    }

    @Test
    public void testComPortInfoDeserializationWithEmptyString() throws Exception {
        String json = "{" +
                "    'type': 'inbound_SERIAL'," +
                "    'comPortType': {'id': 'TYPE_SERIAL'}," +
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
                "    'type': 'inbound_SERIAL'," +
                "    'comPortType': {'id': 'TYPE_SERIAL'}," +
                "    'id': 201," +
                "    'direction': 'inbound'," +
                "    'name': 'comport name'," +
                "    'flowControl': {'id': 'fyk'}" +
                "}";
        json = json.replace("'","\"");
        ComPortInfo response = objectMapper.readValue(json, InboundComPortInfo.class);

    }
}
