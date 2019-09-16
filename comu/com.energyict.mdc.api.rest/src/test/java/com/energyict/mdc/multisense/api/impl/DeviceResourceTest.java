/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.DeviceBuilder;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ExecutableActionPropertyImpl;
import com.energyict.mdc.dynamic.DateFactory;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/29/15.
 */
public class DeviceResourceTest extends MultisensePublicApiJerseyTest {

    private Device deviceXas;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        DeviceType water = mockDeviceType(10, "water", 3333L);
        DeviceType gas = mockDeviceType(11, "gas", 3333L);
        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
        DeviceType elec2 = mockDeviceType(101, "Electricity 2", 3333L);
        DeviceType elec3 = mockDeviceType(101, "Electricity 3", 3333L);
        DeviceType elec4 = mockDeviceType(101, "Electricity 4", 3333L);
        DeviceType elec5 = mockDeviceType(101, "Electricity 5", 3333L);
        Finder<DeviceType> deviceTypeFinder = mockFinder(Arrays.asList(water, gas, elec1, elec2, elec3, elec4, elec5));
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);

        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(13L, "Default configuration", elec1, 3333L);
        Device device = mockDevice("DAV", "65749846514", deviceConfiguration, 3333L);
        deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 223L);
        DeviceConfiguration deviceConfiguration2 = mockDeviceConfiguration(23L, "Default configuration", elec2, 3333L);
        Device device3 = mockDevice("PIO", "54687651356", deviceConfiguration2, 3333L);
        Finder<Device> deviceFinder = mockFinder(Arrays.asList(device, deviceXas, device3));
        when(this.deviceService.findAllDevices(any(Condition.class))).thenReturn(deviceFinder);
    }

    @Test
    public void testDeviceFields() throws Exception {
        Response response = target("/devices").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(25);
        assertThat(model.<List<String>>get("$")).containsOnly("actions", "batch", "connectionMethods", "deviceConfiguration",
                "deviceProtocolPluggeableClassId", "gatewayType", "id", "isDirectlyAddressable", "isGateway", "lifecycleState",
                "link", "mRID", "name", "physicalGateway", "serialNumber","manufacturer", "modelNumber", "modelVersion",
                "slaveDevices", "version", "yearOfCertification", "communicationTaskExecutions", "deviceMessages", "usagePoint", "meterRole");
    }

    @Test
    public void testHypermediaLinkWithConnectionMethods() throws Exception {
        ConnectionTask<?, ?> connectionTask13 = mock(ConnectionTask.class);
        when(connectionTask13.getId()).thenReturn(13L);
        ConnectionTask<?, ?> connectionTask14 = mock(ConnectionTask.class);
        when(connectionTask14.getId()).thenReturn(14L);
        when(deviceXas.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask13, connectionTask14));
        Response response = target("/devices/XAS").queryParam("fields","connectionMethods").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$.connectionMethods")).hasSize(2);
        assertThat(model.<Integer>get("$.connectionMethods[0].id")).isEqualTo(13);
        assertThat(model.<String>get("$.connectionMethods[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.connectionMethods[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/connectiontasks/13");
    }

    @Test
    public void testHypermediaLinkWithFieldsCallSingle() throws Exception {
        Response response = target("/devices/XAS").queryParam("fields", "id,serialNumber").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
    }

    @Test
    public void testGetDeviceExecutableActionsWithStringProperties() throws Exception {
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, stringPropertySpec);
        ExecutableAction executableAction2 = mockExecutableAction(2L, "action.name.2", MicroAction.ENABLE_VALIDATION, stringPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Arrays.asList(executableAction1, executableAction2));
        PropertyValueConverter converter = mock(PropertyValueConverter.class);
        when(converter.getPropertyType(stringPropertySpec)).thenReturn(SimplePropertyType.TEXT);
        when(propertyValueInfoService.getConverter(stringPropertySpec)).thenReturn(converter);
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("value", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/XAS/actions?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(1);
        assertThat(model.<String>get("data[0].name")).isEqualTo("action.name.1");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/actions/1");
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(2);
        assertThat(model.<String>get("data[1].name")).isEqualTo("action.name.2");
        assertThat(model.<String>get("data[1].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[1].link.href")).isEqualTo("http://localhost:9998/devices/XAS/actions/2");
        assertThat(model.<List>get("data[1].properties")).hasSize(1);
        assertThat(model.<String>get("data[1].properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("data[1].properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<String>get("data[1].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[1].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testTriggerDeviceExecutableActionsWithStringProperties() throws Exception {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, stringPropertySpec);
        ExecutableAction executableAction2 = mockExecutableAction(2L, "action.name.2", MicroAction.ENABLE_VALIDATION, stringPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Arrays.asList(executableAction1, executableAction2));
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        info.device = new LinkInfo();
        info.device.version = 223L;
        info.name = "action.name.1";
        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo();
        info.properties.add(propertyInfo);
        propertyInfo.key = "string.property";
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXT;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("abcdefg", null);
        when(deviceLifeCycleService.toExecutableActionProperty("abcdefg", stringPropertySpec)).
                thenAnswer(invocationOnMock -> new ExecutableActionPropertyImpl((PropertySpec) invocationOnMock.getArguments()[1], invocationOnMock.getArguments()[0]));

        Response response = target("/devices/XAS/actions/1").request("application/json").put(Entity.json(info));
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executableAction1).execute(instantArgumentCaptor.capture(), listArgumentCaptor.capture());
        List<ExecutableActionProperty> value = listArgumentCaptor.getValue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).getValue()).isEqualTo("abcdefg");
        assertThat(value.get(0).getPropertySpec().getName()).isEqualTo("string.property");
        assertThat(value.get(0).getPropertySpec().getValueFactory().getClass()).isEqualTo(StringFactory.class);
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(now);
    }

    @Test
    public void testTriggerDeviceExecutableActionsWithEffectiveTime() throws Exception {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now.minusSeconds(60));
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, stringPropertySpec);
        ExecutableAction executableAction2 = mockExecutableAction(2L, "action.name.2", MicroAction.ENABLE_VALIDATION, stringPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Arrays.asList(executableAction1, executableAction2));
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        info.device = new LinkInfo();
        info.device.version = 223L;
        info.name = "action.name.1";
        info.properties = new ArrayList<>();
        info.effectiveTimestamp = now;
        PropertyInfo propertyInfo = new PropertyInfo();
        info.properties.add(propertyInfo);
        propertyInfo.key = "string.property";
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXT;
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("abcdefg", null);
        when(deviceLifeCycleService.toExecutableActionProperty("abcdefg", stringPropertySpec)).
                thenAnswer(invocationOnMock -> new ExecutableActionPropertyImpl((PropertySpec) invocationOnMock.getArguments()[1], invocationOnMock.getArguments()[0]));

        Response response = target("/devices/XAS/actions/1").request("application/json").put(Entity.json(info));
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executableAction1).execute(instantArgumentCaptor.capture(), listArgumentCaptor.capture());
        List<ExecutableActionProperty> value = listArgumentCaptor.getValue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).getValue()).isEqualTo("abcdefg");
        assertThat(value.get(0).getPropertySpec().getName()).isEqualTo("string.property");
        assertThat(value.get(0).getPropertySpec().getValueFactory().getClass()).isEqualTo(StringFactory.class);
        assertThat(instantArgumentCaptor.getValue()).isEqualTo(now);
    }

    @Test
    public void testGetDeviceExecutableActionsWithBigDecimalProperties() throws Exception {
        PropertySpec bigDecimalPropertySpec = mockBigDecimalPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, bigDecimalPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        PropertyValueConverter converter = mock(PropertyValueConverter.class);
        when(converter.getPropertyType(bigDecimalPropertySpec)).thenReturn(SimplePropertyType.TEXT);
        when(propertyValueInfoService.getConverter(bigDecimalPropertySpec)).thenReturn(converter);
        PropertyInfo propertyInfo = new PropertyInfo("decimal.property", "decimal.property", new PropertyValueInfo<>(3, 1), new PropertyTypeInfo(SimplePropertyType.NUMBER, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("decimal.property");
        assertThat(model.<Integer>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo(1);
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testGetDeviceExecutableActionsWithListProperties() throws Exception {
        PropertySpec listPropertySpec = mockExhaustiveListPropertySpec();
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, listPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        PropertyValueConverter converter = mock(PropertyValueConverter.class);
        when(converter.getPropertyType(listPropertySpec)).thenReturn(SimplePropertyType.TEXT);
        when(propertyValueInfoService.getConverter(listPropertySpec)).thenReturn(converter);
        PropertyInfo propertyInfo = new PropertyInfo("list.property", "list.property", new PropertyValueInfo<>("value", "Value1"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("list.property");
        assertThat(model.<String>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo("Value1");
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testGetDeviceExecutableActionsWithDateProperties() throws Exception {
        Date date = new Date();
        PropertySpec listPropertySpec = mockDatePropertySpec(date);
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, listPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        PropertyValueConverter converter = mock(PropertyValueConverter.class);
        when(converter.getPropertyType(listPropertySpec)).thenReturn(com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType.DATE);
        when(propertyValueInfoService.getConverter(listPropertySpec)).thenReturn(converter);
        PropertyInfo propertyInfo = new PropertyInfo("date.property", "date.property", new PropertyValueInfo<>("value", date.getTime()), new PropertyTypeInfo(com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType.DATE, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("date.property");
        assertThat(model.<Long>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo(date.getTime());
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("DATE");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testTriggerDeviceExecutableActionsWithDateProperty() throws Exception {
        Date now = Date.from(LocalDateTime.of(2015, 7, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        PropertySpec datePropertySpec = mockDatePropertySpec(now);
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, datePropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        LifeCycleActionInfo info = new LifeCycleActionInfo();
        info.device = new LinkInfo();
        info.device.version = 223L;
        info.name = "action.name.1";
        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo();
        info.properties.add(propertyInfo);
        propertyInfo.key = "date.property";
        propertyInfo.propertyTypeInfo = new PropertyTypeInfo();
        propertyInfo.propertyValueInfo = new PropertyValueInfo<>("2015-07-08", null);
        when(deviceLifeCycleService.toExecutableActionProperty(now, datePropertySpec)).
                thenAnswer(invocationOnMock -> new ExecutableActionPropertyImpl((PropertySpec) invocationOnMock.getArguments()[1], invocationOnMock.getArguments()[0]));

        Response response = target("/devices/XAS/actions/1").request("application/json").put(Entity.json(info));
        ArgumentCaptor<Instant> instantArgumentCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<List> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(executableAction1).execute(instantArgumentCaptor.capture(), listArgumentCaptor.capture());
        List<ExecutableActionProperty> value = listArgumentCaptor.getValue();
        assertThat(value).hasSize(1);
        assertThat(value.get(0).getValue()).isEqualTo(now);
        assertThat(value.get(0).getPropertySpec().getName()).isEqualTo("date.property");
        assertThat(value.get(0).getPropertySpec().getValueFactory().getClass()).isEqualTo(DateFactory.class);
    }


    @Test
    public void testGetDeviceExecutableActionsWithDateTimeProperties() throws Exception {
        Date date = new Date();
        PropertySpec listPropertySpec = mockDateTimePropertySpec(date);
        ExecutableAction executableAction1 = mockExecutableAction(1L, "action.name.1", MicroAction.ENABLE_ESTIMATION, listPropertySpec);
        when(deviceLifeCycleService.getExecutableActions(deviceXas)).thenReturn(Collections.singletonList(executableAction1));
        PropertyValueConverter converter = mock(PropertyValueConverter.class);
        when(converter.getPropertyType(listPropertySpec)).thenReturn(com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType.CLOCK);
        when(propertyValueInfoService.getConverter(listPropertySpec)).thenReturn(converter);
        PropertyInfo propertyInfo = new PropertyInfo("datetime.property", "datetime.property", new PropertyValueInfo<>("value", date.getTime()), new PropertyTypeInfo(com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType.CLOCK, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/actions").queryParam("start", 0L).queryParam("limit", 10L).request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("data[0].properties")).hasSize(1);
        assertThat(model.<String>get("data[0].properties[0].key")).isEqualTo("datetime.property");
        assertThat(model.<Long>get("data[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo(date.getTime());
        assertThat(model.<String>get("data[0].properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("CLOCK");
        assertThat(model.<Boolean>get("data[0].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testDeviceTypeWithConfig() throws Exception {
        DeviceType serial = mockDeviceType(4, "Serial", 3333L);
        DeviceType serial2 = mockDeviceType(6, "Serial 2", 3333L);
        Finder<DeviceType> finder = mockFinder(Arrays.asList(serial, serial2));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        target("/devicetypes").queryParam("fields", "deviceConfigurations").request("application/json").get();

    }

    @Test
    public void testCreatedeviceWithFindingApplicableDeviceConfiguration() {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        DeviceInfo info = new DeviceInfo();
        info.yearOfCertification = 2017;
        info.deviceConfiguration = new DeviceConfigurationInfo();
        LinkInfo deviceTypeInfo = new LinkInfo();
        deviceTypeInfo.id = "testDeviceType";
        info.deviceConfiguration.deviceType = deviceTypeInfo;
        info.serialNumber = "12345";
        info.usagePoint = "5ba4bd20-3173-43ac-a45b-032f00771b85";
        info.meterRole = "meter.role.Default";
        info.shipmentDate = now;

        DeviceType deviceType = mockDeviceType(123, "testDeviceType", 12345L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1234, "Default", deviceType, 3333L);
        when(deviceConfiguration.isActive()).thenReturn(true);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceTypeByName("testDeviceType")).thenReturn(Optional.of(deviceType));
        UsagePoint usagePoint = mockUsagePoint(123L, "Usage Point", 345L, ServiceKind.ELECTRICITY);
        when(meteringService.findUsagePointByMRID(info.usagePoint)).thenReturn(Optional.of(usagePoint));
        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn(info.meterRole);
        when(metrologyConfigurationService.findMeterRole(info.meterRole)).thenReturn(Optional.of(meterRole));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(134L, "testMC", 345L);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(usagePoint.getEffectiveMetrologyConfigurations(any())).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.emptyList());
        when(effectiveMetrologyConfigurationOnUsagePoint.getRange()).thenReturn(Range.all());
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
        Device newDevice = mockDevice("4dac4bd90-2673-488c-342b-032628771b85", info.serialNumber, deviceConfiguration, 123L);
        DeviceBuilder deviceBuilder = FakeBuilder.initBuilderStub(newDevice, DeviceBuilder.class);
        when(deviceService.newDeviceBuilder(eq(deviceConfiguration), any(String.class), any(Instant.class))).thenReturn(deviceBuilder);
        when(newDevice.getCurrentMeterActivation()).thenReturn(Optional.empty());
        when(newDevice.getUsagePoint()).thenReturn(Optional.empty());
        when(newDevice.getDeviceType()).thenReturn(deviceType);
        CIMLifecycleDates lifecycleDates = mock(CIMLifecycleDates.class);
        when(lifecycleDates.getInstalledDate()).thenReturn(Optional.empty());
        when(newDevice.getLifecycleDates()).thenReturn(lifecycleDates);

        Response response = target("/devices").request("application/json").post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(201);
        verify(deviceService).newDeviceBuilder(eq(deviceConfiguration), any(String.class), any(Instant.class));
        verify(deviceBuilder).create();
    }

    private ExecutableAction mockExecutableAction(Long id, String name, MicroAction microAction, PropertySpec... propertySpecs) {
        ExecutableAction mock = mock(ExecutableAction.class);
        AuthorizedTransitionAction transitionAction = mock(AuthorizedTransitionAction.class);
        when(transitionAction.getId()).thenReturn(id);
        when(transitionAction.getName()).thenReturn(name);
        for (PropertySpec propertySpec : propertySpecs) {
            when(deviceLifeCycleService.getPropertySpecsFor(microAction)).thenReturn(Collections.singletonList(propertySpec));
        }
        when(transitionAction.getActions()).thenReturn(Collections.singleton(microAction));
        when(mock.getAction()).thenReturn(transitionAction);

        return mock;
    }

}
