package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.protocols.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.protocols.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.protocols.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.protocols.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.channels.ip.socket.TcpIpPostDialConnectionType;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class DeviceCommunicationProtocolsResourceTest extends JerseyTest {

    private static NlsService nlsService;
    private static Thesaurus thesaurus;
    private static Clock clock;
    private static DeviceDataService deviceDataService;
    private static DeviceConfigurationService deviceConfigurationService;
    private static TaskService taskService;
    private static PropertySpecService propertySpecService;
    private static ProtocolPluggableService protocolPluggableService;
    private static LicensedProtocolService licensedProtocolService;
    private static MdcPropertyUtils mdcPropertyUtils;
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private DeviceProtocol deviceProtocol;
    private ConnectionType outboundConnectionType1;
    private ConnectionTypePluggableClass connectionTypePluggableCass1;
    private ConnectionType outboundConnectionType2;
    private ConnectionTypePluggableClass connectionTypePluggableCass2;
    private ConnectionType outboundConnectionType3;
    private ConnectionTypePluggableClass connectionTypePluggableCass3;
    private ConnectionType inConnectionType1;
    private ConnectionTypePluggableClass connectionTypePluggableCass4;
    private ConnectionType inConnectionType2;
    private ConnectionTypePluggableClass connectionTypePluggableCass5;
    private List<ConnectionType> deviceProtocolSupportedConnectionTypes;
    private List<ConnectionType> allConnectionTypes;
    private List<ConnectionTypePluggableClass> allConnectionTypePluggableClasses;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deviceDataService = mock(DeviceDataService.class);
        deviceConfigurationService = mock(DeviceConfigurationService.class);
        taskService = mock(TaskService.class);
        clock = mock(Clock.class);
        nlsService = mock(NlsService.class);
        thesaurus = mock(Thesaurus.class);
        propertySpecService = mock(PropertySpecService.class);
        protocolPluggableService = mock(ProtocolPluggableService.class);
        licensedProtocolService = mock(LicensedProtocolService.class);
        mdcPropertyUtils = mock(MdcPropertyUtils.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset();

        deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class, RETURNS_DEEP_STUBS);
        deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        outboundConnectionType1 = new OutboundTcpIpConnectionType();//mockOutboundConnectionType(OutboundTcpIpConnectionType.class);
        connectionTypePluggableCass1 = createMockedConnectionTypePluggableCass(outboundConnectionType1);
        outboundConnectionType2 = new OutboundUdpConnectionType();// mockOutboundConnectionType(OutboundUdpConnectionType.class);
        connectionTypePluggableCass2 = createMockedConnectionTypePluggableCass(outboundConnectionType2);
        outboundConnectionType3 = new TcpIpPostDialConnectionType();//mockOutboundConnectionType(TcpIpPostDialConnectionType.class);
        connectionTypePluggableCass3 = createMockedConnectionTypePluggableCass(outboundConnectionType3);
        inConnectionType1 = new InboundIpConnectionType();//mockInboundConnectionType(InboundIpConnectionType.class);
        connectionTypePluggableCass4 = createMockedConnectionTypePluggableCass(inConnectionType1);
        inConnectionType2 = new EIWebConnectionType();// mockInboundConnectionType(EIWebConnectionType.class);
        connectionTypePluggableCass5 = createMockedConnectionTypePluggableCass(inConnectionType2);

        deviceProtocolSupportedConnectionTypes = Arrays.asList(outboundConnectionType1, outboundConnectionType3, inConnectionType2);
        allConnectionTypes = Arrays.asList(outboundConnectionType1, outboundConnectionType2, outboundConnectionType3, inConnectionType1, inConnectionType2);
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(deviceProtocolSupportedConnectionTypes);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyInt())).thenReturn(deviceProtocolPluggableClass);
        allConnectionTypePluggableClasses = Arrays.asList(connectionTypePluggableCass1, connectionTypePluggableCass2, connectionTypePluggableCass3, connectionTypePluggableCass4, connectionTypePluggableCass5);
        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(allConnectionTypePluggableClasses);

    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                DeviceCommunicationProtocolsResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(deviceDataService).to(DeviceDataService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(taskService).to(TaskService.class);
                bind(nlsService).to(NlsService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(clock).to(Clock.class);
                bind(propertySpecService).to(PropertySpecService.class);
                bind(protocolPluggableService).to(ProtocolPluggableService.class);
                bind(licensedProtocolService).to(LicensedProtocolService.class);
                bind(mdcPropertyUtils).to(MdcPropertyUtils.class);
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
    public void getSupportedConnectionTypesTest() {
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").request().get(List.class);

        assertThat(response).hasSize(3);
    }

    @Test
    public void getOutboundConnectionTypesTest() throws UnsupportedEncodingException {
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").queryParam("filter", ExtjsFilter.filter().property("direction", "outbound").create()).request().get(List.class);

        assertThat(response).hasSize(2);
    }

    @Test
    public void getInboundConnectionTypesTest() throws UnsupportedEncodingException {
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").queryParam("filter", ExtjsFilter.filter().property("direction", "inbound").create()).request().get(List.class);

        assertThat(response).hasSize(1);
    }

    @Test
    public void getSupportedConnectionTypesWhenNoneAreDefinedInTheSystemTest() {
        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Collections.<ConnectionTypePluggableClass>emptyList());
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").request().get(List.class);

        assertThat(response).hasSize(0);
    }

    @Test
    public void getSupportedConnectionTypesWhenDeviceProtocolDoesNotSupportConnectionTypesTest() {
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(Collections.<ConnectionType>emptyList());
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").request().get(List.class);

        assertThat(response).hasSize(0);
    }

    @Test
    public void getOutboundConnectionTypesWhenDeviceProtocolDoesNotSupportOutboundConnectionTypesTest() throws UnsupportedEncodingException {
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(Arrays.asList(inConnectionType1, inConnectionType2));
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").queryParam("filter", ExtjsFilter.filter().property("direction", "outbound").create()).request().get(List.class);

        assertThat(response).hasSize(0);
    }

    @Test
    public void getInboundConnectionTypesWhenDeviceProtocolDoesNotSupportInboundConnectionTypesTest() throws UnsupportedEncodingException {
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(Arrays.asList(outboundConnectionType1, outboundConnectionType2));
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").queryParam("filter", ExtjsFilter.filter().property("direction", "inbound").create()).request().get(List.class);

        assertThat(response).hasSize(0);
    }

    @Test
    public void getConnectionTypesWithIncorrectDirectionReturnsAllConnectionTypesOfDeviceProtocolTest() throws UnsupportedEncodingException {
        List<Map<String, Object>> response = target("/devicecommunicationprotocols/1/connectiontypes").queryParam("filter", ExtjsFilter.filter().property("direction", "ThisIsNotADirectionItIsOnlyForGuidance").create()).request().get(List.class);

        assertThat(response).hasSize(3);
    }

    private ConnectionTypePluggableClass createMockedConnectionTypePluggableCass(ConnectionType connectionType) {
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getConnectionType()).thenReturn(connectionType);
        when(connectionTypePluggableClass.getJavaClassName()).thenReturn(connectionType.getClass().getCanonicalName());
        return connectionTypePluggableClass;
    }
}