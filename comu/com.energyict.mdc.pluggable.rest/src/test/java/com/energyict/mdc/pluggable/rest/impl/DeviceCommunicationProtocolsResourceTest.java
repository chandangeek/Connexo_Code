/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    public void getConnectionTypesKeyAccessorTypePossibleValueInjectionTest() throws IOException {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.isReference()).thenReturn(true);
        ValueFactory referenceValueFactory = mock(ValueFactory.class);
        when(referenceValueFactory.getValueType()).thenReturn(KeyAccessorType.class);
        when(propertySpec.getValueFactory()).thenReturn(referenceValueFactory);

        ConnectionType tlsConnectionType = mock(ConnectionType.class);
        when(tlsConnectionType.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(tlsConnectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
        List<ConnectionType> deviceProtocolSupportedConnectionTypes = Collections.singletonList(tlsConnectionType);
        when(deviceProtocol.getSupportedConnectionTypes()).thenReturn(deviceProtocolSupportedConnectionTypes);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        DeviceType deviceType = mock(DeviceType.class);
        KeyAccessorType kat1 = mockKeyAccessorType("key1", 1L);
        KeyAccessorType kat2 = mockKeyAccessorType("key2", 2L);
        KeyAccessorType kat3 = mockKeyAccessorType("key3", 3L);
        when(deviceType.getKeyAccessorTypes()).thenReturn(Arrays.asList(kat1, kat2, kat3));
        when(deviceConfig.getDeviceType()).thenReturn(deviceType);
        when(deviceConfigurationService.findDeviceConfiguration(12345)).thenReturn(Optional.of(deviceConfig));
        ConnectionTypePluggableClass connectionTypePluggableCass = createMockedConnectionTypePluggableCass(tlsConnectionType);
        when(connectionTypePluggableCass.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Collections.singletonList(connectionTypePluggableCass));
        when(propertyValueInfoService.getPropertyInfo(any(PropertySpec.class), any(Function.class))).thenReturn(new PropertyInfo("someProperty", "Key", new PropertyValueInfo<Object>(), new PropertyTypeInfo(), false));
        PropertyValueConverter propertyValueConverter = mock(PropertyValueConverter.class);
        when(propertyValueConverter.convertValueToInfo(any(PropertySpec.class), any(KeyAccessorType.class))).thenAnswer(invocationOnMock -> {
            return new IdWithNameInfo(((KeyAccessorType)invocationOnMock.getArguments()[1]).getId(),((KeyAccessorType)invocationOnMock.getArguments()[1]).getName());
        });
        when(propertyValueInfoService.getConverter(any(PropertySpec.class))).thenReturn(propertyValueConverter);
        when(connectionTypePluggableCass.getProperties(any(List.class))).thenReturn(TypedProperties.empty());

        Response response = target("/devicecommunicationprotocols/1/connectiontypes").queryParam("filter", ExtjsFilter.filter().property("direction", "outbound").property("deviceConfigId", 12345L).create()).request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());

        assertThat(jsonModel.<String>get("[0].name")).startsWith("com.energyict.mdc.protocol.api.ConnectionType");
        assertThat(jsonModel.<List>get("[0].properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues")).hasSize(3);
        assertThat(jsonModel.<Integer>get("[0].properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("[0].properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[0].name")).isEqualTo("key1");
        assertThat(jsonModel.<Integer>get("[0].properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("[0].properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[1].name")).isEqualTo("key2");
        assertThat(jsonModel.<Integer>get("[0].properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[2].id")).isEqualTo(3);
        assertThat(jsonModel.<String>get("[0].properties[0].propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[2].name")).isEqualTo("key3");
    }

    private KeyAccessorType mockKeyAccessorType(String name, long id) {
        KeyAccessorType kat1 = mock(KeyAccessorType.class);
        when(kat1.getName()).thenReturn(name);
        when(kat1.getId()).thenReturn(id);
        return kat1;
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
        when(connectionTypePluggableClass.getName()).thenReturn(connectionType.getClass().getCanonicalName());
        when(connectionTypePluggableClass.getId()).thenReturn((long) connectionType.getClass().hashCode());
        return connectionTypePluggableClass;
    }

}