package com.energyict.mdc.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.mdc.rest.impl.comserver.ComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComServerResource;
import com.energyict.mdc.rest.impl.comserver.OnlineComServerInfo;
import com.energyict.mdc.rest.impl.comserver.OutboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.TcpInboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.TcpOutboundComPortInfo;
import com.energyict.mdc.rest.impl.comserver.UdpInboundComPortInfo;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.assertj.core.data.MapEntry;

import com.google.common.base.Optional;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ComPortResourceTest extends JerseyTest {

    private static final String COMPORTS_RESOURCE_URL = "/comports"; // if you need to change this URL, API changed!!
    private static EngineModelService engineModelService;

    @BeforeClass
    static public void setUpClass() throws Exception {
        engineModelService = mock(EngineModelService.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset(engineModelService);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(ComServerResource.class, ComPortResource.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(engineModelService).to(EngineModelService.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

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
        when(engineModelService.findAllComPortsWithDeleted()).thenReturn(comPorts);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'data'").containsKey("data").containsKey("total").hasSize(2);
        List<Map<String, Object>> comports = (List<Map<String, Object>>) response.get("data");
        Map<String, Object> comport = comports.get(0);
        assertThat(comport).contains(MapEntry.entry("name", "portname"), MapEntry.entry("id", 1));
    }

    @Test
    public void testGetNonExistingComPortReturns404() throws Exception {
        final Response response = target("/comports/3").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetTcpInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE-2L;
        long comServer_id = Long.MAX_VALUE-113L;
        long comPortPool_id = Long.MAX_VALUE-114L;

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

        when(engineModelService.findComPort(comPort_id)).thenReturn(tcpBasedInboundComPort);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL+"/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "tcp inbound"),
                MapEntry.entry("comPortType", "TCP"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("comPortPool_id", comPortPool_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("portNumber", 8),

                MapEntry.entry("direction", "inbound")
        );
    }

    @Test
    public void testGetServletInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE-12;
        long comServer_id = Long.MAX_VALUE-131L;
        long comPortPool_id = Long.MAX_VALUE-141L;

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

        when(engineModelService.findComPort(comPort_id)).thenReturn(servletBasedInboundComPort);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL+"/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "servlet inbound"),
                MapEntry.entry("comPortType", "SERVLET"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("comPortPool_id", comPortPool_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("useHttps", true),
                MapEntry.entry("keyStoreFilePath", "/path/to/key/store"),
                MapEntry.entry("trustStoreFilePath", "/path/to/trust/store"),
                MapEntry.entry("trustStorePassword", "trustpwd"),
                MapEntry.entry("keyStorePassword", "keypwd"),
                MapEntry.entry("portNumber", 8),
                MapEntry.entry("contextPath", "/context/path"),
                MapEntry.entry("direction", "inbound")
        );
    }

    @Test
    public void testGetUdpInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE;
        long comServer_id = Long.MAX_VALUE-113L;
        long comPortPool_id = Long.MAX_VALUE-116L;

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

        when(engineModelService.findComPort(comPort_id)).thenReturn(udpBasedInboundComPort);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL+"/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "udp inbound"),
                MapEntry.entry("comPortType", "UDP"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("comPortPool_id", comPortPool_id),
                MapEntry.entry("numberOfSimultaneousConnections", 7),
                MapEntry.entry("portNumber", 8),
                MapEntry.entry("bufferSize", 9),

                MapEntry.entry("direction", "inbound")
        );
    }

    @Test
    public void testCanSerializeEmptyModemComPort() throws Exception {
        int comPort_id = 666;
        ModemBasedInboundComPort modemBasedInboundComPort = mock(ModemBasedInboundComPort.class);
        when(modemBasedInboundComPort.getComPortType()).thenReturn(ComPortType.SERIAL);
        when(engineModelService.findComPort(comPort_id)).thenReturn(modemBasedInboundComPort);
        target(COMPORTS_RESOURCE_URL+"/" + comPort_id).request().get(Map.class);
    }

    @Test
    public void testGetModemInboundComPort() throws Exception {
        long comPort_id = Long.MAX_VALUE -13L;
        long comServer_id = Long.MAX_VALUE-113L;
        long comPortPool_id = Long.MAX_VALUE-115L;

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
        when(modemBasedInboundComPort.getAtCommandTimeout()).thenReturn(atCommandTimeout);
        when(modemBasedInboundComPort.getSerialPortConfiguration()).thenReturn(new SerialPortConfiguration("modem inbound", BaudrateValue.BAUDRATE_1200, NrOfDataBits.FIVE, NrOfStopBits.TWO, Parities.EVEN, FlowControl.XONXOFF));

        when(engineModelService.findComPort(comPort_id)).thenReturn(modemBasedInboundComPort);
        final Map<String, Object> response = target(COMPORTS_RESOURCE_URL+"/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> map2 = new HashMap<String, String>();
        map.put("modemInitString", "comme ci");
        map2.put("modemInitString", "comme ca");
        assertThat(response).contains(
                MapEntry.entry("id", comPort_id),
                MapEntry.entry("name", "modem inbound"),
                MapEntry.entry("comPortType", "SERIAL"),
                MapEntry.entry("description", "this is a test port"),
                MapEntry.entry("comServer_id", comServer_id),
                MapEntry.entry("comPortPool_id", comPortPool_id),
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
                MapEntry.entry("modemInitStrings", Arrays.asList(map, map2)),
                MapEntry.entry("baudrate", "1200"),
                MapEntry.entry("nrOfDataBits", "5"),
                MapEntry.entry("nrOfStopBits", "2"),
                MapEntry.entry("parity", "Even parity"),
                MapEntry.entry("flowControl", "Xon/Xoff"),

                MapEntry.entry("direction", "inbound")
        );
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
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(10,11,12));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }


    @Test
    public void testGetComPortsWithComServerAFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target(COMPORTS_RESOURCE_URL).queryParam("filter", ExtjsFilter.filter("comserver_id", "16")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("data");
        List<Map<String, Object>> comPorts = (List) response.get("data");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(12,11));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }

    @Test
    public void testGetComPortsWithComServerBFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target(COMPORTS_RESOURCE_URL).queryParam("filter", ExtjsFilter.filter("comserver_id", "61")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("data");
        List<Map<String, Object>> comPorts = (List) response.get("data");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(13,10));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }

    @Test
    public void createOutboundComPortInSinglePoolTest() {
        long comServerId = 3;

        OnlineComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServerId);
        when(engineModelService.findComServer(comServerId)).thenReturn(Optional.<ComServer>of(serverSideComServer));

        Long poolId = 1543621L;
        OutboundComPortPool myTestOutboundPool = mock(OutboundComPortPool.class);
        when(engineModelService.findOutboundComPortPool(poolId)).thenReturn(myTestOutboundPool);

        when(serverSideComServer.newOutboundComPort(anyString(), anyInt())).thenReturn(new MockOutboundComPortBuilder());

        OutboundComPortInfo outboundComPortInfo = new TcpOutboundComPortInfo();
        outboundComPortInfo.name = "MyOutboundComPort";
        outboundComPortInfo.outboundComPortPoolIds = Arrays.asList(poolId);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name = "MyName";
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = Arrays.asList(outboundComPortInfo);

        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        Response response = target("/comservers/3").request().put(json); // put comserver
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(myTestOutboundPool).addOutboundComPort(any(OutboundComPort.class));
    }

    @Test
    public void createOutboundComPortInMultiplePoolsTest() {
        long comServerId = 3;

        OnlineComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServerId);
        when(engineModelService.findComServer(comServerId)).thenReturn(Optional.<ComServer>of(serverSideComServer));

        Long poolId1 = 1543621L;
        Long poolId2 = 14654111L;
        OutboundComPortPool myTestOutboundPool1 = mock(OutboundComPortPool.class);
        OutboundComPortPool myTestOutboundPool2 = mock(OutboundComPortPool.class);
        when(engineModelService.findOutboundComPortPool(poolId1)).thenReturn(myTestOutboundPool1);
        when(engineModelService.findOutboundComPortPool(poolId2)).thenReturn(myTestOutboundPool2);

        when(serverSideComServer.newOutboundComPort(anyString(), anyInt())).thenReturn(new MockOutboundComPortBuilder());

        OutboundComPortInfo outboundComPortInfo = new TcpOutboundComPortInfo();
        outboundComPortInfo.name = "MyOutboundComPort";
        outboundComPortInfo.outboundComPortPoolIds = Arrays.asList(poolId1, poolId2);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name = "MyName";
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = Arrays.asList(outboundComPortInfo);

        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        Response response = target("/comservers/3").request().put(json); // put comserver
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(myTestOutboundPool1).addOutboundComPort(any(OutboundComPort.class));
        verify(myTestOutboundPool2).addOutboundComPort(any(OutboundComPort.class));
    }

    @Test
    public void updateExistingComPortWithMoreAndLessPoolsTest() {
        long comServerId = 3;
        long comPortId = 31;

        OnlineComServer serverSideComServer = mock(OnlineComServer.class);
        when(serverSideComServer.getId()).thenReturn(comServerId);
        when(engineModelService.findComServer(comServerId)).thenReturn(Optional.<ComServer>of(serverSideComServer));
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        when(outboundComPort.getId()).thenReturn(comPortId);
        when(outboundComPort.getComPortType()).thenReturn(ComPortType.TCP);
        when(serverSideComServer.getComPorts()).thenReturn(Arrays.<ComPort>asList(outboundComPort));

        Long poolId1 = 1543621L;
        Long poolId2 = 14654111L;
        Long poolId3 = 65461413L;
        OutboundComPortPool myTestOutboundPool1 = mock(OutboundComPortPool.class);
        when(myTestOutboundPool1.getId()).thenReturn(poolId1);
        OutboundComPortPool myTestOutboundPool2 = mock(OutboundComPortPool.class);
        when(myTestOutboundPool2.getId()).thenReturn(poolId2);
        OutboundComPortPool myTestOutboundPool3 = mock(OutboundComPortPool.class);
        when(myTestOutboundPool3.getId()).thenReturn(poolId3);
        when(engineModelService.findOutboundComPortPool(poolId1)).thenReturn(myTestOutboundPool1);
        when(engineModelService.findOutboundComPortPool(poolId2)).thenReturn(myTestOutboundPool2);
        when(engineModelService.findOutboundComPortPool(poolId3)).thenReturn(myTestOutboundPool3);
        when(engineModelService.findContainingComPortPoolsForComPort(outboundComPort)).thenReturn(Arrays.asList(myTestOutboundPool1, myTestOutboundPool2));

        when(serverSideComServer.newOutboundComPort(anyString(), anyInt())).thenReturn(new MockOutboundComPortBuilder());

        OutboundComPortInfo outboundComPortInfo = new TcpOutboundComPortInfo();
        outboundComPortInfo.id = comPortId;
        outboundComPortInfo.name = "MyOutboundComPort";
        outboundComPortInfo.outboundComPortPoolIds = Arrays.asList(poolId2, poolId3);

        OnlineComServerInfo onlineComServerInfo = new OnlineComServerInfo();
        onlineComServerInfo.name = "MyName";
        onlineComServerInfo.inboundComPorts = new ArrayList<>();
        onlineComServerInfo.outboundComPorts = Arrays.asList(outboundComPortInfo);

        Entity<OnlineComServerInfo> json = Entity.json(onlineComServerInfo);
        Response response = target("/comservers/3").request().put(json); // put comserver
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(myTestOutboundPool1).removeOutboundComPort(any(OutboundComPort.class));
        verify(myTestOutboundPool3).addOutboundComPort(any(OutboundComPort.class));
        verify(myTestOutboundPool2, never()).addOutboundComPort(any(OutboundComPort.class));
        verify(myTestOutboundPool2, never()).removeOutboundComPort(any(OutboundComPort.class));
    }

    private void setUpComPortFiltering() {
        ComServer comServerA = mock(ComServer.class);
        when(comServerA.getId()).thenReturn(61L);
        ComServer comServerB = mock(ComServer.class);
        when(comServerB.getId()).thenReturn(16L);
        when(engineModelService.findComServer(61)).thenReturn(Optional.of(comServerA));
        when(engineModelService.findComServer(16)).thenReturn(Optional.of(comServerB));
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
        when(engineModelService.findAllComPortsWithDeleted()).thenReturn(comPorts);
        when(engineModelService.findAllInboundComPorts()).thenReturn(Arrays.asList(tcpBasedInboundComPort, udpBasedInboundComPort, modemBasedInboundComPort));
        when(engineModelService.findAllOutboundComPorts()).thenReturn(Arrays.asList(outboundComPort));
        when(engineModelService.findComPortsByComServer(comServerA)).thenReturn(Arrays.asList(tcpBasedInboundComPort, outboundComPort));
        when(engineModelService.findComPortsByComServer(comServerB)).thenReturn(Arrays.<ComPort>asList(modemBasedInboundComPort, udpBasedInboundComPort));
        when(comServerA.getComPorts()).thenReturn(comPorts);
        when(comServerB.getComPorts()).thenReturn(comPorts);
    }

    private Map<String, Object> asMapValue(TimeDuration timeDuration) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("count", timeDuration.getCount());
        map.put("timeUnit", TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode()));
        return map;
    }
}