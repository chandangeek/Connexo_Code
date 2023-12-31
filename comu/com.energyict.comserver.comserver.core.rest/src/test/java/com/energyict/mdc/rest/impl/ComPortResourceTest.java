/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.FlowControl;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.channel.serial.Parities;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.rest.impl.comserver.TcpInboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.TranslationKeys;
import com.energyict.mdc.rest.impl.comserver.UdpInboundComPortInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComPortResourceTest extends ComserverCoreApplicationJerseyTest {

    private static final String COMPORTS_RESOURCE_URL = "/comports"; // if you need to change this URL, API changed!!

    @Test
    public void testCanCreateEmptyTcpInboundComPort() throws Exception {
        TCPBasedInboundComPort tcpBasedInboundComPort = mock(TCPBasedInboundComPort.class);
        new TcpInboundComPortInfo(tcpBasedInboundComPort);
    }

    @Test
    public void testCanCreateEmptyUdpInboundComPort() throws Exception {
        UDPBasedInboundComPort udpBasedInboundComPort = mock(UDPBasedInboundComPort.class);
        new UdpInboundComPortInfo(udpBasedInboundComPort);
    }

    @Test
    public void testGetAllComPorts() throws Exception {
        TCPBasedInboundComPort tcpBasedInboundComPort = mock(TCPBasedInboundComPort.class);
        when(tcpBasedInboundComPort.getId()).thenReturn(1L);
        when(tcpBasedInboundComPort.getName()).thenReturn("portname");
        when(tcpBasedInboundComPort.getComPortType()).thenReturn(ComPortType.TCP);

        List<ComPort> comPorts = new ArrayList<>();
        comPorts.add(tcpBasedInboundComPort);
        when(engineConfigurationService.findAllComPortsIncludingObsolete()).thenReturn(comPorts);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'data'").containsKey("data").containsKey("total").hasSize(2);
        List<Map<String, Object>> comports = (List<Map<String, Object>>) response.get("data");
        Map<String, Object> comport = comports.get(0);
        assertThat(comport).contains(MapEntry.entry("name", "portname"), MapEntry.entry("id", 1));
    }

    @Test
    public void testGetNonExistingComPortReturns404() throws Exception {
        doReturn(Optional.empty()).when(engineConfigurationService).findComPort(anyLong());
        final Response response = target("/comports/3").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetTcpInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE - 2L;
        long comServer_id = Long.MAX_VALUE - 113L;
        long comPortPool_id = Long.MAX_VALUE - 114L;

        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(comServer_id);

        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(comPortPool_id);

        TCPBasedInboundComPort tcpBasedInboundComPort = mock(TCPBasedInboundComPort.class);
        when(tcpBasedInboundComPort.getId()).thenReturn(comPort_id);
        when(tcpBasedInboundComPort.getName()).thenReturn("tcp inbound");
        when(tcpBasedInboundComPort.getComPortType()).thenReturn(ComPortType.TCP);
        when(tcpBasedInboundComPort.getDescription()).thenReturn("this is a test port");
        when(tcpBasedInboundComPort.getComServer()).thenReturn(comServer);
        when(tcpBasedInboundComPort.getComPortPool()).thenReturn(comPortPool);
        when(tcpBasedInboundComPort.getNumberOfSimultaneousConnections()).thenReturn(7);
        when(tcpBasedInboundComPort.getPortNumber()).thenReturn(8);

        ObjectMapper objectMapper = new ObjectMapper();
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
        AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.setAnnotationIntrospector(pair);

        doReturn(Optional.of(tcpBasedInboundComPort)).when(engineConfigurationService).findComPort(comPort_id);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL + "/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "tcp inbound"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("portNumber", 8),

                MapEntry.entry("direction", TranslationKeys.COMPORT_INBOUND.getDefaultFormat())
        );
        assertThat(response).containsKey("comPortPool_id");
        try {
            String responseString = objectMapper.writeValueAsString(response.get("comPortType"));
            assertThat(responseString).contains("{\"id\":\"TYPE_TCP\",\"localizedValue\":\"TCP\"}");
        } catch (Exception ex) {}
    }

    @Test
    public void testGetServletInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE - 12;
        long comServer_id = Long.MAX_VALUE - 131L;
        long comPortPool_id = Long.MAX_VALUE - 141L;

        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(comServer_id);

        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(comPortPool_id);

        ServletBasedInboundComPort servletBasedInboundComPort = mock(ServletBasedInboundComPort.class);
        when(servletBasedInboundComPort.getId()).thenReturn(comPort_id);
        when(servletBasedInboundComPort.getName()).thenReturn("servlet inbound");
        when(servletBasedInboundComPort.getComPortType()).thenReturn(ComPortType.SERVLET);
        when(servletBasedInboundComPort.getDescription()).thenReturn("this is a test port");
        when(servletBasedInboundComPort.getComServer()).thenReturn(comServer);
        when(servletBasedInboundComPort.getComPortPool()).thenReturn(comPortPool);
        when(servletBasedInboundComPort.getNumberOfSimultaneousConnections()).thenReturn(7);
        when(servletBasedInboundComPort.getPortNumber()).thenReturn(8);
        when(servletBasedInboundComPort.isHttps()).thenReturn(true);
        when(servletBasedInboundComPort.getContextPath()).thenReturn("/context/path");
        when(servletBasedInboundComPort.getTrustStoreSpecsFilePath()).thenReturn("/path/to/trust/store");
        when(servletBasedInboundComPort.getTrustStoreSpecsPassword()).thenReturn("trustpwd");
        when(servletBasedInboundComPort.getKeyStoreSpecsFilePath()).thenReturn("/path/to/key/store");
        when(servletBasedInboundComPort.getKeyStoreSpecsPassword()).thenReturn("keypwd");

        doReturn(Optional.of(servletBasedInboundComPort)).when(engineConfigurationService).findComPort(comPort_id);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL + "/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS

        ObjectMapper objectMapper = new ObjectMapper();
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
        AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.setAnnotationIntrospector(pair);

        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "servlet inbound"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("useHttps", true),
                MapEntry.entry("keyStoreFilePath", "/path/to/key/store"),
                MapEntry.entry("trustStoreFilePath", "/path/to/trust/store"),
                MapEntry.entry("trustStorePassword", "trustpwd"),
                MapEntry.entry("keyStorePassword", "keypwd"),
                MapEntry.entry("portNumber", 8),
                MapEntry.entry("contextPath", "/context/path"),
                MapEntry.entry("direction", TranslationKeys.COMPORT_INBOUND.getDefaultFormat())
        );
        assertThat(response).containsKey("comPortPool_id");
        try {
            String responseString = objectMapper.writeValueAsString(response.get("comPortType"));
            assertThat(responseString).contains("{\"id\":\"TYPE_SERVLET\",\"localizedValue\":\"SERVLET\"}");
        } catch (Exception ex) {
        }
    }

    @Test
    public void testGetCoapInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE - 12;
        long comServer_id = Long.MAX_VALUE - 131L;
        long comPortPool_id = Long.MAX_VALUE - 141L;

        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(comServer_id);

        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(comPortPool_id);

        CoapBasedInboundComPort coapBasedInboundComPort = mock(CoapBasedInboundComPort.class);
        when(coapBasedInboundComPort.getId()).thenReturn(comPort_id);
        when(coapBasedInboundComPort.getName()).thenReturn("coap inbound");
        when(coapBasedInboundComPort.getComPortType()).thenReturn(ComPortType.COAP);
        when(coapBasedInboundComPort.getDescription()).thenReturn("this is a test port");
        when(coapBasedInboundComPort.getComServer()).thenReturn(comServer);
        when(coapBasedInboundComPort.getComPortPool()).thenReturn(comPortPool);
        when(coapBasedInboundComPort.getNumberOfSimultaneousConnections()).thenReturn(7);
        when(coapBasedInboundComPort.getPortNumber()).thenReturn(8);
        when(coapBasedInboundComPort.isDtls()).thenReturn(true);
        when(coapBasedInboundComPort.isUsingSharedKeys()).thenReturn(false);
        when(coapBasedInboundComPort.getBufferSize()).thenReturn(1096);
        when(coapBasedInboundComPort.getContextPath()).thenReturn("/context/path");
        when(coapBasedInboundComPort.getTrustStoreSpecsFilePath()).thenReturn("/path/to/trust/store");
        when(coapBasedInboundComPort.getTrustStoreSpecsPassword()).thenReturn("trustpwd");
        when(coapBasedInboundComPort.getKeyStoreSpecsFilePath()).thenReturn("/path/to/key/store");
        when(coapBasedInboundComPort.getKeyStoreSpecsPassword()).thenReturn("keypwd");

        doReturn(Optional.of(coapBasedInboundComPort)).when(engineConfigurationService).findComPort(comPort_id);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL + "/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS

        ObjectMapper objectMapper = new ObjectMapper();
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
        AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.setAnnotationIntrospector(pair);

        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "coap inbound"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("useDtls", true),
                MapEntry.entry("useSharedKeys", false),
                MapEntry.entry("bufferSize", 1096),
                MapEntry.entry("keyStoreFilePath", "/path/to/key/store"),
                MapEntry.entry("trustStoreFilePath", "/path/to/trust/store"),
                MapEntry.entry("trustStorePassword", "trustpwd"),
                MapEntry.entry("keyStorePassword", "keypwd"),
                MapEntry.entry("portNumber", 8),
                MapEntry.entry("contextPath", "/context/path"),
                MapEntry.entry("direction", TranslationKeys.COMPORT_INBOUND.getDefaultFormat())
        );
        assertThat(response).containsKey("comPortPool_id");
        try {
            String responseString = objectMapper.writeValueAsString(response.get("comPortType"));
            assertThat(responseString).contains("{\"id\":\"TYPE_COAP\",\"localizedValue\":\"COAP\"}");
        } catch (Exception ex) {
        }
    }

    @Test
    public void testGetUdpInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE;
        long comServer_id = Long.MAX_VALUE - 113L;
        long comPortPool_id = Long.MAX_VALUE - 116L;

        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(comServer_id);

        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(comPortPool_id);

        UDPBasedInboundComPort udpBasedInboundComPort = mock(UDPBasedInboundComPort.class);
        when(udpBasedInboundComPort.getId()).thenReturn(comPort_id);
        when(udpBasedInboundComPort.getName()).thenReturn("udp inbound");
        when(udpBasedInboundComPort.getComPortType()).thenReturn(ComPortType.UDP);
        when(udpBasedInboundComPort.getDescription()).thenReturn("this is a test port");
        when(udpBasedInboundComPort.getComServer()).thenReturn(comServer);
        when(udpBasedInboundComPort.getComPortPool()).thenReturn(comPortPool);
        when(udpBasedInboundComPort.getNumberOfSimultaneousConnections()).thenReturn(7);
        when(udpBasedInboundComPort.getPortNumber()).thenReturn(8);
        when(udpBasedInboundComPort.getBufferSize()).thenReturn(9);

        doReturn(Optional.of(udpBasedInboundComPort)).when(engineConfigurationService).findComPort(comPort_id);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL + "/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS

        ObjectMapper objectMapper = new ObjectMapper();
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
        AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.setAnnotationIntrospector(pair);

        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "udp inbound"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("portNumber", 8),
                MapEntry.entry("bufferSize", 9),

                MapEntry.entry("direction", TranslationKeys.COMPORT_INBOUND.getDefaultFormat())
        );
        assertThat(response).containsKey("comPortPool_id");
        try {
            String responseString = objectMapper.writeValueAsString(response.get("comPortType"));
            assertThat(responseString).contains("{\"id\":\"TYPE_UDP\",\"localizedValue\":\"UDP\"}");
        } catch (Exception ex) {}
    }

    @Test
    public void testCanSerializeEmptyModemComPort() throws Exception {
        int comPort_id = 666;
        ModemBasedInboundComPort modemBasedInboundComPort = mock(ModemBasedInboundComPort.class);
        when(modemBasedInboundComPort.getComPortType()).thenReturn(ComPortType.SERIAL);
        doReturn(Optional.of(modemBasedInboundComPort)).when(engineConfigurationService).findComPort(comPort_id);
        target(COMPORTS_RESOURCE_URL + "/" + comPort_id).request().get(Map.class);
    }

    @Test
    public void testGetModemInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE - 13L;
        long comServer_id = Long.MAX_VALUE - 113L;
        long comPortPool_id = Long.MAX_VALUE - 115L;

        TimeDuration delayBeforeSend = new TimeDuration("8 seconds");
        TimeDuration connectTimeout = new TimeDuration("9 minutes");
        TimeDuration delayAfterConnect = new TimeDuration("10 hours");
        TimeDuration atCommandTimeout = new TimeDuration("11 days");

        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(comServer_id);

        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getId()).thenReturn(comPortPool_id);

        ModemBasedInboundComPort modemBasedInboundComPort = mock(ModemBasedInboundComPort.class);
        when(modemBasedInboundComPort.getId()).thenReturn(comPort_id);
        when(modemBasedInboundComPort.getName()).thenReturn("modem inbound");
        when(modemBasedInboundComPort.getComPortType()).thenReturn(ComPortType.SERIAL);
        when(modemBasedInboundComPort.getDescription()).thenReturn("this is a test port");
        when(modemBasedInboundComPort.getComServer()).thenReturn(comServer);
        when(modemBasedInboundComPort.getComPortPool()).thenReturn(comPortPool);
        when(modemBasedInboundComPort.getNumberOfSimultaneousConnections()).thenReturn(7);
        when(modemBasedInboundComPort.getDelayBeforeSend()).thenReturn(delayBeforeSend);
        when(modemBasedInboundComPort.getAddressSelector()).thenReturn("allo allo");
        when(modemBasedInboundComPort.getAtCommandTry()).thenReturn(BigDecimal.valueOf(123));
        when(modemBasedInboundComPort.getConnectTimeout()).thenReturn(connectTimeout);
        when(modemBasedInboundComPort.getDelayAfterConnect()).thenReturn(delayAfterConnect);
        when(modemBasedInboundComPort.getMaximumDialErrors()).thenReturn(10);
        when(modemBasedInboundComPort.getPostDialCommands()).thenReturn("byebye");
        when(modemBasedInboundComPort.getRingCount()).thenReturn(11);
        when(modemBasedInboundComPort.getModemInitStrings()).thenReturn(Arrays.asList("comme ci", "comme ca"));
        when(modemBasedInboundComPort.getGlobalModemInitStrings()).thenReturn(Arrays.asList("G1", "G2"));
        when(modemBasedInboundComPort.getAtCommandTimeout()).thenReturn(atCommandTimeout);
        when(modemBasedInboundComPort.getSerialPortConfiguration()).thenReturn(new SerialPortConfiguration("modem inbound", BaudrateValue.BAUDRATE_1200, NrOfDataBits.FIVE, NrOfStopBits.TWO, Parities.EVEN, FlowControl.XONXOFF));

        doReturn(Optional.of(modemBasedInboundComPort)).when(engineConfigurationService).findComPort(comPort_id);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL + "/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> map2 = new HashMap<>();
        map.put("modemInitString", "comme ci");
        map2.put("modemInitString", "comme ca");

        HashMap<String, String> map3 = new HashMap<>();
        HashMap<String, String> map4 = new HashMap<>();
        map3.put("globalModemInitString", "G1");
        map4.put("globalModemInitString", "G2");

        ObjectMapper objectMapper = new ObjectMapper();
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(objectMapper.getTypeFactory());
        AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.setAnnotationIntrospector(pair);

        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "modem inbound"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("addressSelector", "allo allo"),
                MapEntry.entry("atCommandTry", 123),
                MapEntry.entry("delayBeforeSend", asMapValue(delayBeforeSend)),
                MapEntry.entry("connectTimeout", asMapValue(connectTimeout)),
                MapEntry.entry("delayAfterConnect", asMapValue(delayAfterConnect)),
                MapEntry.entry("atCommandTimeout", asMapValue(atCommandTimeout)),
                MapEntry.entry("maximumNumberOfDialErrors", 10),
                MapEntry.entry("postDialCommands", "byebye"),
                MapEntry.entry("ringCount", 11),
                MapEntry.entry("globalModemInitStrings", Arrays.asList(map3, map4)),
                MapEntry.entry("modemInitStrings", Arrays.asList(map, map2)),
                MapEntry.entry("baudrate", "1200"),
                MapEntry.entry("nrOfDataBits", "5"),
                MapEntry.entry("nrOfStopBits", "2"),
                MapEntry.entry("direction", TranslationKeys.COMPORT_INBOUND.getDefaultFormat())
        );
        assertThat(response).containsKey("comPortPool_id");
        try {
            String responseString = objectMapper.writeValueAsString(response.get("comPortType"));
            assertThat(responseString).contains("{\"id\":\"TYPE_SERIAL\",\"localizedValue\":\"SERIAL\"}");
            responseString = objectMapper.writeValueAsString(response.get("parity"));
            assertThat(responseString).contains("{\"id\":\"parities_even\",\"localizedValue\":\"Even Parity\"}");
            responseString = objectMapper.writeValueAsString(response.get("flowControl"));
            assertThat(responseString).contains("{\"id\":\"flowcontrol_xon_xoff\",\"localizedValue\":\"XON/XOFF\"}");
        } catch (Exception ex) {}
    }

    @Test
    public void testGetOutboundComPortsWithFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target(COMPORTS_RESOURCE_URL).queryParam("filter", ExtjsFilter.filter("direction", "outbound")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("data");
        List<Map<String, Object>> comPorts = (List) response.get("data");
        Map<String, Object> foundPort = comPorts.get(0);
        assertThat(foundPort.get("id")).isEqualTo(13);
    }

    @Test
    public void testGetInboundComPortsWithFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target(COMPORTS_RESOURCE_URL).queryParam("filter", ExtjsFilter.filter("direction", "inbound")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("data");
        List<Map<String, Object>> comPorts = (List) response.get("data");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(10, 11, 12));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }


    @Test
    public void testGetComPortsWithComServerAFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target(COMPORTS_RESOURCE_URL).queryParam("filter", ExtjsFilter.filter("comserver_id", 16l)).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("data");
        List<Map<String, Object>> comPorts = (List) response.get("data");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(12, 11));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }

    @Test
    public void testGetComPortsWithComServerBFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target(COMPORTS_RESOURCE_URL).queryParam("filter", ExtjsFilter.filter("comserver_id", 61l)).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("data");
        List<Map<String, Object>> comPorts = (List) response.get("data");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(13, 10));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }

    private void setUpComPortFiltering() {
        ComServer comServerA = mock(ComServer.class);
        when(comServerA.getId()).thenReturn(61L);
        ComServer comServerB = mock(ComServer.class);
        when(comServerB.getId()).thenReturn(16L);
        when(engineConfigurationService.findComServer(61)).thenReturn(Optional.of(comServerA));
        when(engineConfigurationService.findComServer(16)).thenReturn(Optional.of(comServerB));
        TCPBasedInboundComPort tcpBasedInboundComPort = mock(TCPBasedInboundComPort.class);
        when(tcpBasedInboundComPort.getId()).thenReturn(10L);
        when(tcpBasedInboundComPort.getComServer()).thenReturn(comServerA);
        when(tcpBasedInboundComPort.getComPortType()).thenReturn(ComPortType.TCP);
        UDPBasedInboundComPort udpBasedInboundComPort = mock(UDPBasedInboundComPort.class);
        when(udpBasedInboundComPort.getId()).thenReturn(11L);
        when(udpBasedInboundComPort.getComServer()).thenReturn(comServerB);
        when(udpBasedInboundComPort.getComPortType()).thenReturn(ComPortType.UDP);
        ModemBasedInboundComPort modemBasedInboundComPort = mock(ModemBasedInboundComPort.class);
        when(modemBasedInboundComPort.getId()).thenReturn(12L);
        when(modemBasedInboundComPort.getComServer()).thenReturn(comServerB);
        when(modemBasedInboundComPort.getComPortType()).thenReturn(ComPortType.SERIAL);
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        when(outboundComPort.getId()).thenReturn(13L);
        when(outboundComPort.getComServer()).thenReturn(comServerA);
        when(outboundComPort.getComPortType()).thenReturn(ComPortType.TCP);
        List<ComPort> comPorts = new ArrayList<>();
        comPorts.add(tcpBasedInboundComPort);
        comPorts.add(udpBasedInboundComPort);
        comPorts.add(modemBasedInboundComPort);
        comPorts.add(outboundComPort);
        when(engineConfigurationService.findAllComPortsIncludingObsolete()).thenReturn(comPorts);
        when(engineConfigurationService.findAllInboundComPorts()).thenReturn(Arrays.asList(tcpBasedInboundComPort, udpBasedInboundComPort, modemBasedInboundComPort));
        when(engineConfigurationService.findAllOutboundComPorts()).thenReturn(Arrays.asList(outboundComPort));
        when(engineConfigurationService.findComPortsByComServer(comServerA)).thenReturn(Arrays.asList(tcpBasedInboundComPort, outboundComPort));
        when(engineConfigurationService.findComPortsByComServer(comServerB)).thenReturn(Arrays.asList(modemBasedInboundComPort, udpBasedInboundComPort));
        when(comServerA.getComPorts()).thenReturn(comPorts);
        when(comServerB.getComPorts()).thenReturn(comPorts);
    }

    private Map<String, Object> asMapValue(TimeDuration timeDuration) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("count", timeDuration.getCount());
        map.put("timeUnit", TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode()));
        map.put("localizedTimeUnit", TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode()));
        map.put("asSeconds", timeDuration.getSeconds());
        return map;
    }
}
