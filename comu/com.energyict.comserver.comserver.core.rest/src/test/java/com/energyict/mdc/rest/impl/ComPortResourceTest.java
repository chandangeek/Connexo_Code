package com.energyict.mdc.rest.impl;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.ports.InboundComPortPool;
import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.ports.ServletBasedInboundComPort;
import com.energyict.mdc.ports.TCPBasedInboundComPort;
import com.energyict.mdc.ports.UDPBasedInboundComPort;
import com.energyict.mdc.servers.ComServer;
import com.energyict.mdc.services.ComPortService;
import com.energyict.mdc.services.ComServerService;
import com.energyict.mdc.shadow.ports.KeyStoreShadow;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.assertj.core.data.MapEntry;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComPortResourceTest extends JerseyTest {

    private static ComServerService comServerService;
    private static ComPortService comPortService;

    @BeforeClass
    static public void setUpClass() throws Exception {
        comServerService = mock(ComServerService.class);
        comPortService = mock(ComPortService.class);
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(ComPortResource.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(comServerService).to(ComServerService.class);
                bind(comPortService).to(ComPortService.class);
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
        when(tcpBasedInboundComPort.getId()).thenReturn(1);
        when(tcpBasedInboundComPort.getName()).thenReturn("portname");
        List<ComPort> comPorts = new ArrayList<>();
        comPorts.add(tcpBasedInboundComPort);
        when(comPortService.findAll()).thenReturn(comPorts);
        final Map<String, Object> response = target("/comports").request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).describedAs("Should contain field 'ComPorts'").containsKey("ComPorts").hasSize(1);
        List<Map<String, Object>> comports = (List<Map<String, Object>>) response.get("ComPorts");
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
        int comPort_id = 2;
        int comServer_id = 113;
        int comPortPool_id = 114;

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

        when(comPortService.find(comPort_id)).thenReturn(tcpBasedInboundComPort);
        final Map<String, Object> response = target("/comports/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
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
        int comPort_id = 12;
        int comServer_id = 131;
        int comPortPool_id = 141;

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
        when(servletBasedInboundComPort.useHttps()).thenReturn(true);
        when(servletBasedInboundComPort.getTrustedKeyStoreSpecifications()).thenReturn(new KeyStoreShadow("/path/to/trust/store", "trustpwd"));
        when(servletBasedInboundComPort.getKeyStoreSpecifications()).thenReturn(new KeyStoreShadow("/path/to/key/store", "keypwd"));

        when(comPortService.find(comPort_id)).thenReturn(servletBasedInboundComPort);
        final Map<String, Object> response = target("/comports/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
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

                MapEntry.entry("direction", "inbound")
        );
    }

    @Test
    public void testGetUdpInboundComPort() throws Exception {
        int comPort_id = 2;
        int comServer_id = 113;
        int comPortPool_id = 116;

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

        when(comPortService.find(comPort_id)).thenReturn(udpBasedInboundComPort);
        final Map<String, Object> response = target("/comports/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
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
        when(comPortService.find(comPort_id)).thenReturn(modemBasedInboundComPort);
        target("/comports/" + comPort_id).request().get(Map.class);
    }

    @Test
    public void testGetModemInboundComPort() throws Exception {
        int comPort_id = 13;
        int comServer_id = 113;
        int comPortPool_id = 115;
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
        when(modemBasedInboundComPort.getMaximumNumberOfDialErrors()).thenReturn(10);
        when(modemBasedInboundComPort.getPostDialCommands()).thenReturn("byebye");
        when(modemBasedInboundComPort.getRingCount()).thenReturn(11);
        when(modemBasedInboundComPort.getModemInitStrings()).thenReturn(Arrays.asList("comme ci", "comme ca"));
        when(modemBasedInboundComPort.getAtCommandTimeout()).thenReturn(atCommandTimeout);
        when(modemBasedInboundComPort.getSerialPortConfiguration()).thenReturn(new SerialPortConfiguration("port name", BaudrateValue.BAUDRATE_1200, NrOfDataBits.FIVE, NrOfStopBits.TWO, Parities.EVEN, FlowControl.XONXOFF));

        when(comPortService.find(comPort_id)).thenReturn(modemBasedInboundComPort);
        final Map<String, Object> response = target("/comports/" + comPort_id).request().get(Map.class); // Using MAP instead of *Info to resemble JS
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
                MapEntry.entry("modemInitStrings", Arrays.asList("comme ci", "comme ca")),
                MapEntry.entry("comPortName", "port name"),
                MapEntry.entry("baudrate", "BAUDRATE_1200"),
                MapEntry.entry("nrOfDataBits", "FIVE"),
                MapEntry.entry("nrOfStopBits", "TWO"),
                MapEntry.entry("parity", "EVEN"),
                MapEntry.entry("flowControl", "XONXOFF"),

                MapEntry.entry("direction", "inbound")
        );
    }

    @Test
    public void testGetOutboundComPortsWithFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target("/comports").queryParam("filter", filter("direction", "outbound")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("ComPorts");
        List<Map<String, Object>> comPorts = (List) response.get("ComPorts");
        Map<String, Object> foundPort = comPorts.get(0);
        assertThat(foundPort.get("id")).isEqualTo(13);
    }

    @Test
    public void testGetInboundComPortsWithFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target("/comports").queryParam("filter", filter("direction", "inbound")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("ComPorts");
        List<Map<String, Object>> comPorts = (List) response.get("ComPorts");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(10,11,12));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }


    @Test
    public void testGetComPortsWithComServerAFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target("/comports").queryParam("filter", filter("comserver_id", "16")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("ComPorts");
        List<Map<String, Object>> comPorts = (List) response.get("ComPorts");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(12,11));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }

    @Test
    public void testGetComPortsWithComServerBFilter() throws Exception {
        setUpComPortFiltering();

        final Map response = target("/comports").queryParam("filter", filter("comserver_id", "61")).request().get(Map.class); // Using MAP instead of *Info to resemble JS
        assertThat(response).containsKey("ComPorts");
        List<Map<String, Object>> comPorts = (List) response.get("ComPorts");
        List<Integer> requiredIds = new ArrayList<>(Arrays.asList(13,10));
        for (Map<String, Object> comPort : comPorts) {
            assertThat(requiredIds.remove(comPort.get("id")));
        }
        assertThat(requiredIds).describedAs("A required comport was not return by filter").isEmpty();
    }

    private void setUpComPortFiltering() {
        ComServer comServerA = mock(ComServer.class);
        when(comServerA.getId()).thenReturn(61);
        ComServer comServerB = mock(ComServer.class);
        when(comServerB.getId()).thenReturn(16);
        when(comServerService.find(61)).thenReturn(comServerA);
        when(comServerService.find(16)).thenReturn(comServerB);
        TCPBasedInboundComPort tcpBasedInboundComPort = mock(TCPBasedInboundComPort.class);
        when(tcpBasedInboundComPort.getId()).thenReturn(10);
        when(tcpBasedInboundComPort.getComServer()).thenReturn(comServerA);
        UDPBasedInboundComPort udpBasedInboundComPort = mock(UDPBasedInboundComPort.class);
        when(udpBasedInboundComPort.getId()).thenReturn(11);
        when(udpBasedInboundComPort.getComServer()).thenReturn(comServerB);
        ModemBasedInboundComPort modemBasedInboundComPort = mock(ModemBasedInboundComPort.class);
        when(modemBasedInboundComPort.getId()).thenReturn(12);
        when(modemBasedInboundComPort.getComServer()).thenReturn(comServerB);
        OutboundComPort outboundComPort = mock(OutboundComPort.class);
        when(outboundComPort.getId()).thenReturn(13);
        when(outboundComPort.getComServer()).thenReturn(comServerA);
        List<ComPort> comPorts = new ArrayList<>();
        comPorts.add(tcpBasedInboundComPort);
        comPorts.add(udpBasedInboundComPort);
        comPorts.add(modemBasedInboundComPort);
        comPorts.add(outboundComPort);
        when(comPortService.findAll()).thenReturn(comPorts);
        when(comPortService.findAllInboundComPorts()).thenReturn(Arrays.asList(tcpBasedInboundComPort, udpBasedInboundComPort, modemBasedInboundComPort));
        when(comPortService.findAllOutboundComPorts()).thenReturn(Arrays.asList(outboundComPort));
        when(comPortService.findByComServer(comServerA)).thenReturn(Arrays.asList(tcpBasedInboundComPort, outboundComPort));
        when(comPortService.findByComServer(comServerB)).thenReturn(Arrays.<ComPort>asList(modemBasedInboundComPort, udpBasedInboundComPort));
    }

    private String filter(String property, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(String.format("[{\"property\":\"%s\",\"value\":\"%s\"}]", property, value), "UTF-8");
    }

    private Map<String, Object> asMapValue(TimeDuration timeDuration) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("count", timeDuration.getCount());
        map.put("timeUnit", TimeDuration.getTimeUnitDescription(timeDuration.getTimeUnitCode()));
        return map;
    }
}