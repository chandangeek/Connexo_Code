/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceCommunicationProtocolsResourceTest extends PluggableRestApplicationJerseyTest {

    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private SocketService socketService;
    private DeviceProtocol deviceProtocol;
    private ConnectionType outboundConnectionType1;
    private ConnectionType outboundConnectionType2;
    private ConnectionType inConnectionType1;
    private ConnectionType inConnectionType2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset();

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class, RETURNS_DEEP_STUBS);
        deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        outboundConnectionType1 = new DummyOutboundConnectionType1();
        ConnectionTypePluggableClass connectionTypePluggableCass1 = createMockedConnectionTypePluggableCass(outboundConnectionType1);
        outboundConnectionType2 = new DummyOutboundConnectionType2();
        ConnectionTypePluggableClass connectionTypePluggableCass2 = createMockedConnectionTypePluggableCass(outboundConnectionType2);
        ConnectionType outboundConnectionType3 = new DummyOutboundConnectionType3();
        ConnectionTypePluggableClass connectionTypePluggableCass3 = createMockedConnectionTypePluggableCass(outboundConnectionType3);
        inConnectionType1 = new DummyInboundConnectionType1();
        ConnectionTypePluggableClass connectionTypePluggableCass4 = createMockedConnectionTypePluggableCass(inConnectionType1);
        inConnectionType2 = new DummyInboundConnectionType2();
        ConnectionTypePluggableClass connectionTypePluggableCass5 = createMockedConnectionTypePluggableCass(inConnectionType2);

        List<ConnectionType> deviceProtocolSupportedConnectionTypes = Arrays.asList(outboundConnectionType1, outboundConnectionType3, inConnectionType2);
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(deviceProtocolSupportedConnectionTypes);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.of(deviceProtocolPluggableClass));
        List<ConnectionTypePluggableClass> allConnectionTypePluggableClasses = Arrays.asList(connectionTypePluggableCass1, connectionTypePluggableCass2, connectionTypePluggableCass3, connectionTypePluggableCass4, connectionTypePluggableCass5);
        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(allConnectionTypePluggableClasses);

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

    @Test
    public void testDeleteProtocolOkVersion(){
        DeviceProtocolPluggableClass protocolClass = mock(DeviceProtocolPluggableClass.class);
        when(protocolPluggableService.findAndLockDeviceProtocolPluggableClassByIdAndVersion(1L, 1L)).thenReturn(Optional.of(protocolClass));

        DeviceCommunicationProtocolInfo info = new DeviceCommunicationProtocolInfo();
        info.id = 1L;
        info.version = 1L;

        Response response = target("/devicecommunicationprotocols/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(protocolClass).delete();
    }

    @Test
    public void testDeleteProtocolBadVersion(){
        DeviceProtocolPluggableClass protocolClass = mock(DeviceProtocolPluggableClass.class);
        when(protocolPluggableService.findAndLockDeviceProtocolPluggableClassByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(protocolPluggableService.findDeviceProtocolPluggableClass(1L)).thenReturn(Optional.<DeviceProtocolPluggableClass>empty());

        DeviceCommunicationProtocolInfo info = new DeviceCommunicationProtocolInfo();
        info.id = 1L;
        info.version = 1L;

        Response response = target("/devicecommunicationprotocols/1").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(protocolClass, never()).delete();
    }

    private ConnectionTypePluggableClass createMockedConnectionTypePluggableCass(ConnectionType connectionType) {
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getConnectionType()).thenReturn(connectionType);
        when(connectionTypePluggableClass.getJavaClassName()).thenReturn(connectionType.getClass().getCanonicalName());
        return connectionTypePluggableClass;
    }

}