/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.Answers;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.IncompatibleFiniteStateMachineChangeException;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceTypePurpose;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.IncompatibleDeviceLifeCycleChangeException;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.LongStream;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceTypeResourceTest extends DeviceConfigurationApplicationJerseyTest {
    public static final ReadingType READING_TYPE_1 = mockReadingType("0.1.2.3.5.6.7.8.9.1.2.3.4.5.6.7.8");
    public static final ReadingType READING_TYPE_2 = mockReadingType("0.1.2.3.5.6.7.8.9.1.2.3.4.0.0.0.0");

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    Unit unit = Unit.get("kWh");

    @Test
    public void testGetEmptyDeviceTypeList() throws Exception {
        Finder<DeviceType> finder = mockFinder(Collections.<DeviceType>emptyList());
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("deviceTypes")).isEmpty();
    }

    @Test
    public void testGetNonExistingDeviceType() throws Exception {
        when(deviceConfigurationService.findDeviceType(12345)).thenReturn(Optional.empty());
        Response response = target("/devicetypes/12345").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllDeviceTypesWithoutPaging() throws Exception {
        Finder<DeviceType> finder = mockFinder(Arrays.asList(mockDeviceType("device type 1", 66), mockDeviceType("device type 2", 66), mockDeviceType("device type 3", 66), mockDeviceType("device type 4", 66)));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(4);
        assertThat((List) map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testCreateDeviceTypeNonExistingProtocol() throws Exception {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.name = "newName";
        deviceTypeInfo.deviceProtocolPluggableClassName = "theProtocol";
        deviceTypeInfo.deviceTypePurpose = DeviceTypePurpose.REGULAR.name();
        deviceTypeInfo.deviceLifeCycleId = null;
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        DeviceType deviceType = mock(DeviceType.class);
        DeviceType.DeviceTypeBuilder deviceTypeBuilder = mock(DeviceType.DeviceTypeBuilder.class);
        when(deviceTypeBuilder.create()).thenReturn(deviceType);

        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = Optional.empty();
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(protocolPluggableService.findDeviceProtocolPluggableClassByName("theProtocol")).thenReturn(deviceProtocolPluggableClass);
        when(deviceConfigurationService.newDeviceTypeBuilder("newName", null, null)).thenReturn(deviceTypeBuilder);
        Response response = target("/devicetypes/").request().post(json);
        verify(deviceTypeBuilder).create();
    }

    @Test
    public void testCreateDeviceType() throws Exception {
        DeviceLifeCycle deviceLifeCycle = mockStandardDeviceLifeCycle();
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.name = "newName";
        deviceTypeInfo.deviceProtocolPluggableClassName = "theProtocol";
        deviceTypeInfo.deviceLifeCycleId = deviceLifeCycle.getId();
        deviceTypeInfo.deviceTypePurpose = DeviceTypePurpose.REGULAR.name();
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);

        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(deviceLifeCycle));
        DeviceProtocolPluggableClass protocol = mock(DeviceProtocolPluggableClass.class);
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = Optional.of(protocol);
        when(protocolPluggableService.findDeviceProtocolPluggableClassByName("theProtocol")).thenReturn(deviceProtocolPluggableClass);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        DeviceType.DeviceTypeBuilder deviceTypeBuilder = mock(DeviceType.DeviceTypeBuilder.class);
        when(deviceTypeBuilder.create()).thenReturn(deviceType);
        when(deviceConfigurationService.newDeviceTypeBuilder("newName", protocol, deviceLifeCycle)).thenReturn(deviceTypeBuilder);

        Response response = target("/devicetypes/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void createDataloggerSlaveTest() {
        DeviceLifeCycle deviceLifeCycle = mockStandardDeviceLifeCycle();
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.name = "newName";
        deviceTypeInfo.deviceLifeCycleId = deviceLifeCycle.getId();
        deviceTypeInfo.deviceTypePurpose = DeviceTypePurpose.DATALOGGER_SLAVE.name();
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);

        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(Matchers.anyLong())).thenReturn(Optional.of(deviceLifeCycle));
        DeviceType.DeviceTypeBuilder deviceTypeBuilder = mock(DeviceType.DeviceTypeBuilder.class);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        when(deviceTypeBuilder.create()).thenReturn(deviceType);
        when(deviceConfigurationService.newDataloggerSlaveDeviceTypeBuilder("newName", deviceLifeCycle)).thenReturn(deviceTypeBuilder);

        Response response = target("/devicetypes/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAllDeviceTypesWithFullPage() throws Exception {
        Finder<DeviceType> finder = mockFinder(Arrays.asList(mockDeviceType("device type 1", 66), mockDeviceType("device type 2", 66), mockDeviceType("device type 3", 66), mockDeviceType("device type 4", 66)));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 0)
                .queryParam("limit", 4)
                .request()
                .get(Map.class);
        assertThat(map.get("total")).isEqualTo(4);
        assertThat((List) map.get("deviceTypes")).hasSize(4);
    }

    @Test
    public void testGetEmptyDeviceTypeListPaged() throws Exception {
        Finder<DeviceType> finder = mockFinder(Collections.<DeviceType>emptyList());
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").queryParam("start", 100)
                .queryParam("limit", 20)
                .request()
                .get(Map.class);
        assertThat(map.get("total")).isEqualTo(100);
        assertThat((List) map.get("deviceTypes")).isEmpty();
        ArgumentCaptor<JsonQueryParameters> queryParametersArgumentCaptor = ArgumentCaptor.forClass(JsonQueryParameters.class);
        verify(finder).from(queryParametersArgumentCaptor.capture());
        assertThat(queryParametersArgumentCaptor.getValue().getStart().get()).isEqualTo(100);
        assertThat(queryParametersArgumentCaptor.getValue().getLimit().get()).isEqualTo(20);
    }

    @Test
    public void testGetDeviceTypeByName() throws Exception {
        String webRTUKP = "WebRTUKP";
        DeviceType deviceType = mockDeviceType(webRTUKP, 66);
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/66").request().get(Map.class);
        assertThat(map.get("name")).isEqualTo(webRTUKP);
    }

    @Test
    public void testGetLogBookTypesForDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("any", 84);
        when(deviceConfigurationService.findDeviceType(84)).thenReturn(Optional.of(deviceType));

        List logBooksList = new ArrayList();
        logBooksList.add(mockLogBookType(1, "first", "0.0.0.0.1"));
        logBooksList.add(mockLogBookType(2, "second", "0.0.0.0.2"));
        when(deviceType.getLogBookTypes()).thenReturn(logBooksList);

        Map<String, Object> map = target("/devicetypes/84/logbooktypes").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List<Map> logBookTypeInfos = (List) map.get("logbookTypes");
        assertThat(logBookTypeInfos.size()).isEqualTo(2);
        assertThat(logBookTypeInfos.get(0).get("id")).isEqualTo(1);
        assertThat(logBookTypeInfos.get(1).get("obisCode")).isEqualTo("0.0.0.0.2");
    }

    private LogBookType mockLogBookType(long id, String name, String obisCode) {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getId()).thenReturn(id);
        when(logBookType.getName()).thenReturn(name);
        ObisCode obisCodeObj = mock(ObisCode.class);
        when(obisCodeObj.toString()).thenReturn(obisCode);
        when(logBookType.getObisCode()).thenReturn(obisCodeObj);
        return logBookType;
    }

    private DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();
        when(deviceType.getRegisterTypeTypeCustomPropertySet(anyObject())).thenReturn(Optional.of(registeredCustomPropertySet));
        when(deviceType.getCustomPropertySets()).thenReturn(Arrays.asList(registeredCustomPropertySet));
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceLifeCycle deviceLifeCycle = mockStandardDeviceLifeCycle();
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);
        List<DeviceConfiguration> deviceConfigurations = new ArrayList<>();
        when(deviceType.getConfigurations()).thenReturn(deviceConfigurations);
        when(deviceType.getVersion()).thenReturn(OK_VERSION);

        doReturn(Optional.of(deviceType)).when(deviceConfigurationService).findDeviceType(id);
        doReturn(Optional.of(deviceType)).when(deviceConfigurationService).findAndLockDeviceType(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService).findAndLockDeviceType(id, BAD_VERSION);

        return deviceType;
    }

    private DeviceLifeCycle mockStandardDeviceLifeCycle() {
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle.getId()).thenReturn(1L);
        when(deviceLifeCycle.getName()).thenReturn("Default");
        return deviceLifeCycle;
    }

    private DeviceConfiguration mockDeviceConfiguration(long id, DeviceType deviceType) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn("Device configuration " + id);
        when(deviceConfiguration.getId()).thenReturn(id);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        deviceType.getConfigurations().add(deviceConfiguration);
        List<RegisterSpec> registerSpec = new ArrayList<>();
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(registerSpec);
        List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(partialConnectionTasks);

        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService).findDeviceConfiguration(id);
        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(id, BAD_VERSION);

        return deviceConfiguration;
    }


    private DeviceConfiguration mockDeviceConfiguration(String name, long id) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Arrays.asList(registerSpec));

        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService).findDeviceConfiguration(id);
        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(id, BAD_VERSION);

        return deviceConfiguration;
    }

    @Test
    public void testDeviceTypeInfoJavaScriptMappings() throws Exception {
        int NUMBER_OF_CONFIGS = 4;
        int NUMBER_OF_LOADPROFILES = 6;
        int NUMBER_OF_REGISTERS = 8;
        int NUMBER_OF_LOGBOOKS = 10;

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("unique name");
        when(deviceType.getId()).thenReturn(13L);
        List configsList = mock(List.class);
        when(configsList.size()).thenReturn(NUMBER_OF_CONFIGS);
        List loadProfileList = mock(List.class);
        when(loadProfileList.size()).thenReturn(NUMBER_OF_LOADPROFILES);
        List registerList = Arrays.asList(new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo(), new RegisterTypeInfo());
        List logBooksList = mock(List.class);
        when(logBooksList.size()).thenReturn(NUMBER_OF_LOGBOOKS);
        DeviceLifeCycle deviceLifeCycle = mockStandardDeviceLifeCycle();
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getConfigurations()).thenReturn(configsList);
        when(deviceType.getLoadProfileTypes()).thenReturn(loadProfileList);
        when(deviceType.canActAsGateway()).thenReturn(true);
        when(deviceType.isDirectlyAddressable()).thenReturn(true);
        when(deviceType.getLogBookTypes()).thenReturn(logBooksList);
        when(deviceType.getRegisterTypes()).thenReturn(registerList);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));

        Finder<DeviceType> finder = mockFinder(Arrays.asList(deviceType));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        Map<String, Object> map = target("/devicetypes/").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed")
                .isEqualTo(1);
        assertThat((List) map.get("deviceTypes")).hasSize(1)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceType = (Map) ((List) map.get("deviceTypes")).get(0);
        assertThat(jsonDeviceType.get("id")).isEqualTo(13)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("logBookCount")).isEqualTo(NUMBER_OF_LOGBOOKS)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("registerCount")).isEqualTo(NUMBER_OF_REGISTERS)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceConfigurationCount")).isEqualTo(NUMBER_OF_CONFIGS)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("loadProfileCount")).isEqualTo(NUMBER_OF_LOADPROFILES)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("name")).isEqualTo("unique name")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("canBeGateway")).isEqualTo(true)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("canBeDirectlyAddressed")).isEqualTo(true)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceProtocolPluggableClass")).isEqualTo("device protocol name")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceType.get("deviceLifeCycleId")).isEqualTo(1);
        assertThat(jsonDeviceType.get("deviceLifeCycleName")).isEqualTo("Default");
        assertThat(jsonDeviceType.containsKey("registerTypes")).describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testDeviceConfigurationInfoJavaScriptMappings() throws Exception {

        DeviceType deviceType = mock(DeviceType.class);
        List registerList = mock(List.class);
        when(registerList.size()).thenReturn(2);
        List logBookList = mock(List.class);
        when(logBookList.size()).thenReturn(3);
        List loadProfileList = mock(List.class);
        when(loadProfileList.size()).thenReturn(4);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(113L);
        when(deviceConfiguration.getName()).thenReturn("defcon");
        when(deviceConfiguration.isActive()).thenReturn(true);
        when(deviceConfiguration.getDescription()).thenReturn("describe me");
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(registerList);
        when(deviceConfiguration.getLoadProfileSpecs()).thenReturn(loadProfileList);
        when(deviceConfiguration.getLogBookSpecs()).thenReturn(logBookList);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.METER);
        when(deviceConfiguration.canActAsGateway()).thenReturn(true);
        when(deviceConfiguration.getGatewayType()).thenReturn(GatewayType.HOME_AREA_NETWORK);
        when(deviceConfiguration.isDirectlyAddressable()).thenReturn(true);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);

        Finder<DeviceConfiguration> finder = mockFinder(deviceType.getConfigurations());
        when(deviceConfigurationService.findDeviceConfigurationsUsingDeviceType(any(DeviceType.class))).thenReturn(finder);
        when(deviceConfigurationService.findDeviceType(6)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/6/deviceconfigurations").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed")
                .isEqualTo(1);
        assertThat((List) map.get("deviceConfigurations")).hasSize(1)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceConfiguration = (Map) ((List) map.get("deviceConfigurations")).get(0);
        assertThat(jsonDeviceConfiguration).hasSize(16);
        assertThat(jsonDeviceConfiguration.get("id")).isEqualTo(113)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("name")).isEqualTo("defcon")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("active")).isEqualTo(true)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("description")).isEqualTo("describe me")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("deviceProtocolPluggableClass")).isEqualTo("device protocol name")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("deviceFunction")).isEqualTo("Meter")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("registerCount")).isEqualTo(2)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("logBookCount")).isEqualTo(3)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("loadProfileCount")).isEqualTo(4)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("canBeGateway")).isEqualTo(true)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("gatewayType")).isEqualTo("HAN")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("isDirectlyAddressable")).isEqualTo(true)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration.get("version")).isEqualTo(((Number) OK_VERSION).intValue())
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testRegisterConfigurationInfoJavaScriptMappings() throws Exception {
        DeviceType deviceType = mockDeviceType("some", 6);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(113L, deviceType);

        NumericalRegisterSpec registerSpec = mockNumericalRegister(1L, deviceConfiguration);

        Map<String, Object> jsonRegisterConfiguration = target("/devicetypes/6/deviceconfigurations/113/registerconfigurations/1")
                .request()
                .get(Map.class);
        assertThat(jsonRegisterConfiguration.keySet()).containsOnly("id", "name", "readingType", "registerType", "obisCode", "overruledObisCode",
                "obisCodeDescription", "numberOfFractionDigits", "overflow", "asText", "useMultiplier",
                "possibleCalculatedReadingTypes", "collectedReadingType", "version", "parent")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    private NumericalRegisterSpec mockNumericalRegister(long id, DeviceConfiguration deviceConfiguration) {
        ReadingType readingType = mockReadingType();
        when(readingType.getAliasName()).thenReturn("alias");
        when(readingType.getFullAliasName()).thenReturn("alias");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.empty());
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getReadingType()).thenReturn(readingType);

        ObisCode obisCode = mockObisCode();

        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.isTextual()).thenReturn(false);
        when(registerSpec.getId()).thenReturn(id);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerSpec.getDeviceObisCode()).thenReturn(new ObisCode());
        when(registerSpec.getOverflowValue()).thenReturn(Optional.of(BigDecimal.ONE));
        when(registerSpec.getNumberOfFractionDigits()).thenReturn(1);
        when(registerSpec.getObisCode()).thenReturn(obisCode);
        when(registerSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(registerSpec.getVersion()).thenReturn(OK_VERSION);
        when(registerSpec.getReadingType()).thenReturn(readingType);
        deviceConfiguration.getRegisterSpecs().add(registerSpec);

        doReturn(Optional.of(registerSpec)).when(deviceConfigurationService).findRegisterSpec(id);
        doReturn(Optional.of(registerSpec)).when(deviceConfigurationService)
                .findAndLockRegisterSpecByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockRegisterSpecByIdAndVersion(id, BAD_VERSION);

        return registerSpec;
    }

    @Test
    public void testRegisterTypesInfoJavaScriptMappings() throws Exception {
        DeviceType deviceType = mockDeviceType("name", 6);
        RegisterType registerType = mock(RegisterType.class);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType));

        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getDeviceFunction()).thenReturn(DeviceFunction.METER);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getName()).thenReturn("device protocol name");
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        ReadingType readingType = READING_TYPE_1;
        when(registerType.getReadingType()).thenReturn(readingType);

        List<RegisterSpec> registerSpecs = mock(List.class);
        when(registerSpecs.size()).thenReturn(1);
        when(deviceConfigurationService.findActiveRegisterSpecsByDeviceTypeAndRegisterType(deviceType, registerType)).thenReturn(registerSpecs);
        when(deviceConfigurationService.findDeviceType(6)).thenReturn(Optional.of(deviceType));

        Map<String, Object> map = target("/devicetypes/6/registertypes").request().get(Map.class);
        assertThat(map.get("total")).describedAs("JSon representation of a field, JavaScript impact if it changed")
                .isEqualTo(1);
        assertThat((List) map.get("registerTypes")).hasSize(1)
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        Map jsonDeviceConfiguration = (Map) ((List) map.get("registerTypes")).get(0);
        assertThat(jsonDeviceConfiguration).containsKey("isLinkedByActiveRegisterConfig")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
        assertThat(jsonDeviceConfiguration).containsKey("isLinkedByInactiveRegisterConfig")
                .describedAs("JSon representation of a field, JavaScript impact if it changed");
    }

    @Test
    public void testUnlinkSingleNonExistingRegisterTypeFromDeviceType() throws Exception {
        // Backend has RM 101, UI wants to remove 102
        long RM_ID_1 = 101L;

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        when(masterDataService.findRegisterType(101L)).thenReturn(Optional.empty());
        when(masterDataService.findAndLockRegisterTypeByIdAndVersion(101L, OK_VERSION)).thenReturn(Optional.empty());

        RegisterTypeInfo info = new RegisterTypeInfo();
        info.id = 101L;
        info.readingType = new ReadingTypeInfo();
        info.readingType.aliasName = "Bulk";
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(31L, OK_VERSION);
        Response response = target("/devicetypes/31/registertypes/102").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testUpdateRegistersWithoutChanges() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101 and 102: no changes
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);
        RegisterTypeInfo registerTypeInfo2 = new RegisterTypeInfo();
        registerTypeInfo2.id = RM_ID_2;
        registerTypeInfo2.obisCode = new ObisCode(11, 111, 12, 112, 13, 113);

        DeviceType deviceType = mockDeviceType("updater", 31L);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1, registerTypeInfo2);
        deviceTypeInfo.version = OK_VERSION;
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing added
        verify(deviceType, never()).removeRegisterType(any(RegisterType.class));
        verify(deviceType, never()).addRegisterType(any(RegisterType.class));
    }

    @Test
    public void testUpdateRegistersAddOneRegister() throws Exception {
        // Backend has RM 101, UI sets for 101 and 102: 102 should be added
        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);
        RegisterTypeInfo registerTypeInfo2 = new RegisterTypeInfo();
        registerTypeInfo2.id = RM_ID_2;
        registerTypeInfo2.obisCode = new ObisCode(11, 111, 12, 112, 13, 113);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType measurementType101 = mock(RegisterType.class);
        when(measurementType101.getId()).thenReturn(RM_ID_1);
        RegisterType measurementType102 = mock(RegisterType.class);
        when(measurementType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(measurementType101));
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(measurementType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.of(measurementType102));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);

        DeviceTypeInfo info = new DeviceTypeInfo();
        info.registerTypes = Arrays.asList(registerTypeInfo1, registerTypeInfo2);
        info.version = OK_VERSION;
        Entity<DeviceTypeInfo> json = Entity.json(info);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(deviceType, never()).removeRegisterType(any(RegisterType.class));
        verify(deviceType).addRegisterType(measurementType102);
    }

    @Test
    public void testUpdateDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(101L, deviceType);

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo(deviceConfiguration);
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.canBeGateway = true;
        deviceConfigurationInfo.isDirectlyAddressable = true;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration).setName("new name");
        verify(deviceConfiguration).setDirectlyAddressable(true);
        verify(deviceConfiguration).setCanActAsGateway(true);
    }

    @Test
    public void testUpdateDeviceConfigurationWithoutGatewayAndDirectAddrressJsonFields() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(101L, deviceType);

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo(deviceConfiguration);
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.isDirectlyAddressable = null;
        deviceConfigurationInfo.canBeGateway = null;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration).setName("new name");
        verify(deviceConfiguration, never()).setDirectlyAddressable(anyBoolean());
        verify(deviceConfiguration, never()).setCanActAsGateway(anyBoolean());
    }

    @Test
    public void testCreateDeviceConfigurationWithoutGatewayAndDirectAddressJsonFields() throws Exception {
        DeviceType deviceType = mockDeviceType("creater", 31L);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = mock(DeviceType.DeviceConfigurationBuilder.class, Answers.RETURNS_SELF);
        when(deviceType.newConfiguration("new name")).thenReturn(deviceConfigurationBuilder);
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));
        DeviceConfiguration returnValue = mock(DeviceConfiguration.class);
        when(deviceConfigurationBuilder.add()).thenReturn(returnValue);
        when(returnValue.getId()).thenReturn(1L);
        when(returnValue.getDeviceType()).thenReturn(deviceType);
        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.description = "desc";
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfigurationBuilder, never()).canActAsGateway(anyBoolean());
        verify(deviceConfigurationBuilder, never()).isDirectlyAddressable(anyBoolean());
        verify(deviceConfigurationBuilder).description("desc");
    }

    @Test
    public void testCreateDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("creater", 31L);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = mock(DeviceType.DeviceConfigurationBuilder.class, Answers.RETURNS_SELF);
        when(deviceType.newConfiguration("new name")).thenReturn(deviceConfigurationBuilder);
        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));
        DeviceConfiguration returnValue = mock(DeviceConfiguration.class);
        when(deviceConfigurationBuilder.add()).thenReturn(returnValue);
        when(returnValue.getId()).thenReturn(1L);
        when(returnValue.getDeviceType()).thenReturn(deviceType);
        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo();
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.description = "desc";
        deviceConfigurationInfo.canBeGateway = true;
        deviceConfigurationInfo.isDirectlyAddressable = false;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfigurationBuilder).canActAsGateway(true);
        verify(deviceConfigurationBuilder).isDirectlyAddressable(false);
        verify(deviceConfigurationBuilder).description("desc");
    }

    @Test
    public void testCloneDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("clone", 31L);
        DeviceConfiguration deviceConfiguration101 = mock(DeviceConfiguration.class);
        DeviceConfiguration deviceConfiguration102 = mock(DeviceConfiguration.class);

        when(deviceConfiguration101.getId()).thenReturn(101L);
        when(deviceConfiguration101.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration101.getName()).thenReturn("old name");

        when(deviceConfiguration102.getId()).thenReturn(102L);
        when(deviceConfiguration102.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration102.getName()).thenReturn("new name");

        when(deviceConfigurationService.findDeviceType(31L)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional
                .of(deviceConfiguration101));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration101, deviceConfiguration102));

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo(deviceConfiguration101);
        deviceConfigurationInfo.name = "new name";

        when(deviceConfigurationService.cloneDeviceConfiguration(deviceConfiguration101, deviceConfigurationInfo.name)).thenReturn(deviceConfiguration102);
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(deviceConfiguration101.getId()).isNotEqualTo(deviceConfiguration102.getId());
        assertThat(deviceConfiguration101.getName()).isNotEqualTo(deviceConfiguration102.getName());
        assertThat(deviceConfiguration101.getDeviceType()).isEqualTo(deviceConfiguration102.getDeviceType());
    }

    @Test
    public void testDeleteDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(101, deviceType);

        DeviceConfigurationInfo info = new DeviceConfigurationInfo(deviceConfiguration);

        Response response = target("/devicetypes/31/deviceconfigurations/101").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceType).removeConfiguration(deviceConfiguration);
    }

    @Test
    public void testDeleteDeviceConfigurationBadVersion() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(101, deviceType);

        DeviceConfigurationInfo info = new DeviceConfigurationInfo(deviceConfiguration);
        info.version = BAD_VERSION;

        Response response = target("/devicetypes/31/deviceconfigurations/101").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(deviceType, never()).removeConfiguration(deviceConfiguration);
    }

    @Test
    public void testDeleteDeviceConfigurationBadParentVersion() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(101, deviceType);

        DeviceConfigurationInfo info = new DeviceConfigurationInfo(deviceConfiguration);
        info.parent.version = BAD_VERSION;

        Response response = target("/devicetypes/31/deviceconfigurations/101").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(deviceType, never()).removeConfiguration(deviceConfiguration);
    }

    @Test
    public void testDeleteNonExistingDeviceConfiguration() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        when(deviceConfigurationService.findDeviceConfiguration(101L)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(101L, OK_VERSION)).thenReturn(Optional
                .empty());

        DeviceConfigurationInfo info = new DeviceConfigurationInfo();
        info.id = 101;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(31L, OK_VERSION);
        Response response = target("/devicetypes/31/deviceconfigurations/101").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testActivateDeviceConfigurationIgnoresOtherProperties() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(101, deviceType);

        DeviceConfigurationInfo deviceConfigurationInfo = new DeviceConfigurationInfo(deviceConfiguration);
        deviceConfigurationInfo.name = "new name";
        deviceConfigurationInfo.active = true;
        Entity<DeviceConfigurationInfo> json = Entity.json(deviceConfigurationInfo);
        Response response = target("/devicetypes/31/deviceconfigurations/101/status").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration, never()).setName(anyString());
        verify(deviceConfiguration, times(1)).activate();
    }

    @Test
    public void testUpdateRegistersAddNoneExistingRegisterType() throws Exception {
        // Backend has RM 101, UI sets for 101 and 102: 102 should be added but does not exist
        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);
        RegisterTypeInfo registerTypeInfo2 = new RegisterTypeInfo();
        registerTypeInfo2.id = RM_ID_2;
        registerTypeInfo2.obisCode = new ObisCode(11, 111, 12, 112, 13, 113);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(registerType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.empty());
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1, registerTypeInfo2);
        deviceTypeInfo.version = OK_VERSION;
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testUpdateRegistersDeleteOneMapping() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101: delete 102
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.<DeviceProtocolPluggableClass>mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(registerType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.of(registerType102));

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1);
        deviceTypeInfo.version = OK_VERSION;
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // Nothing deleted, nothing updated
        verify(deviceType).removeRegisterType(registerType102);
        verify(deviceType, never()).addRegisterType(any(RegisterType.class));
    }

    @Test
    public void testRegistersForDeviceTypeWithoutFilterAreSorted() throws Exception {
        // Backend has RM 101 and 102 for device type 31
        long deviceType_id = 31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterType registerType101 = mock(RegisterType.class);
        ReadingType readingType1 = READING_TYPE_1;
        ReadingType readingType2 = READING_TYPE_2;
        when(registerType101.getId()).thenReturn(RM_ID_1);
        when(registerType101.getReadingType()).thenReturn(readingType1);
        when(readingType1.getFullAliasName()).thenReturn("zzz");
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(registerType102.getReadingType()).thenReturn(readingType2);
        when(readingType2.getFullAliasName()).thenReturn("aaa");
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));

        Map response = target("/devicetypes/31/registertypes").request().get(Map.class);
        assertThat(response).hasSize(2);
        List<Map> registerTypes = (List) response.get("registerTypes");
        assertThat(registerTypes).hasSize(2);
        assertThat(((Map) registerTypes.get(0)
                .get("readingType")).get("mRID")).isEqualTo("0.1.2.3.5.6.7.8.9.1.2.3.4.0.0.0.0");
        assertThat(((Map) registerTypes.get(1)
                .get("readingType")).get("mRID")).isEqualTo("0.1.2.3.5.6.7.8.9.1.2.3.4.5.6.7.8");
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_Filtered() throws Exception {
        // Backend has RM 101 for device type 31, while 101, 102 and 103 exist in the server.
        long deviceType_id = 31;
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;
        long RM_ID_3 = 103L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = READING_TYPE_1;
        when(registerType101.getReadingType()).thenReturn(readingType);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(registerType102.getReadingType()).thenReturn(readingType);
        RegisterType registerType103 = mock(RegisterType.class);
        when(registerType103.getId()).thenReturn(RM_ID_3);
        when(registerType103.getReadingType()).thenReturn(readingType);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        Finder<RegisterType> registerTypeFinder = mockFinder(Arrays.asList(registerType101, registerType102, registerType103));
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);

        Map response = target("/devicetypes/31/registertypes").queryParam("filter", ExtjsFilter.filter()
                .property("available", "true")
                .create()).request().get(Map.class);
        assertThat(response).hasSize(2);
        List registerTypes = (List) response.get("registerTypes");
        assertThat(registerTypes).hasSize(2);
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_Filtered_unpaged() throws Exception {
        // Backend has RM 101 for device type 31, while 101->130 exist in the server.
        long deviceType_id = 31;
        long RM_ID_1 = 101L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = READING_TYPE_1;

        List<RegisterType> registerTypeList = new ArrayList<>();
        LongStream.range(101, 131).forEach(i -> {
            RegisterType registerType = mock(RegisterType.class);
            when(registerType.getReadingType()).thenReturn(readingType);
            when(registerType.getId()).thenReturn(i);
            registerTypeList.add(registerType);
        });
        registerTypeList.add(registerType101);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        Finder<RegisterType> registerTypeFinder = mockFinder(registerTypeList);
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);

        Response response = target("/devicetypes/31/registertypes").queryParam("filter", ExtjsFilter.filter()
                .property("available", "true")
                .create()).request().get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(29);
        assertThat(jsonModel.<List<RegisterType>>get("$.registerTypes")).hasSize(29);
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_Filtered_2ndPage() throws Exception {
        // Backend has RM 101 for device type 31, while 101->130 exist in the server.
        long deviceType_id = 31;
        long RM_ID_1 = 101L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = READING_TYPE_1;

        List<RegisterType> registerTypeList = new ArrayList<>();
        LongStream.range(101, 131).forEach(i -> {
            RegisterType registerType = mock(RegisterType.class);
            when(registerType.getReadingType()).thenReturn(readingType);
            when(registerType.getId()).thenReturn(i);
            registerTypeList.add(registerType);
        });
        registerTypeList.add(registerType101);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        Finder<RegisterType> registerTypeFinder = mockFinder(registerTypeList);
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);

        Response response = target("/devicetypes/31/registertypes").queryParam("start", 10)
                .queryParam("limit", 10)
                .queryParam("filter", ExtjsFilter.filter().property("available", "true").create())
                .request()
                .get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(21);
        assertThat(jsonModel.<List<RegisterType>>get("$.registerTypes")).hasSize(10);
        assertThat(jsonModel.<List<Integer>>get("$.registerTypes[*].id")).containsOnly(112, 113, 114, 115, 116, 117, 118, 119, 120, 121);
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_FilteredByConfig_unpaged() throws Exception {
        // Backend has RM 101 for device type 31, while 101->130 exist in the server.
        long deviceType_id = 31;
        long RM_ID_1 = 101L;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = READING_TYPE_1;

        List<RegisterType> registerTypeList = new ArrayList<>();
        LongStream.range(101, 131).forEach(i -> {
            RegisterType registerType = mock(RegisterType.class);
            when(registerType.getReadingType()).thenReturn(readingType);
            when(registerType.getId()).thenReturn(i);
            registerTypeList.add(registerType);
        });
        registerTypeList.add(registerType101);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101));
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        Finder<RegisterType> registerTypeFinder = mockFinder(registerTypeList);
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);

        Response response = target("/devicetypes/31/registertypes").queryParam("filter", ExtjsFilter.filter()
                .property("available", "true")
                .create()).request().get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(29);
        assertThat(jsonModel.<List<RegisterType>>get("$.registerTypes")).hasSize(29);
    }

    @Test
    public void testGetAllAvailableRegistersForDeviceType_FilteredByDeviceConfig_2ndPage() throws Exception {
        // Backend has RM 101 for device type 31, while 101->130 exist in the server, all without register spec
        long deviceType_id = 31;
        long RM_ID_1 = 101L;
        long deviceConfiguration_id = 41;

        DeviceType deviceType = mockDeviceType("getUnfiltered", (int) deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", (int) deviceConfiguration_id);

        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        ReadingType readingType = READING_TYPE_1;
        when(registerType101.getReadingType()).thenReturn(readingType);

        List<RegisterType> registerTypeList = new ArrayList<>();
        LongStream.range(101, 131).forEach(i -> {
            RegisterType registerType = mock(RegisterType.class);
            when(registerType.getReadingType()).thenReturn(readingType);
            when(registerType.getId()).thenReturn(i);
            registerTypeList.add(registerType);
        });
        registerTypeList.add(registerType101);
        when(deviceType.getRegisterTypes()).thenReturn(registerTypeList);
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));

        Response response = target("/devicetypes/31/registertypes").queryParam("start", 10)
                .queryParam("limit", 10)
                .queryParam("filter", ExtjsFilter.filter()
                        .property("available", "true")
                        .property("deviceconfigurationid", 41l)
                        .create())
                .request()
                .get();
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(21);
        assertThat(jsonModel.<List<RegisterType>>get("$.registerTypes")).hasSize(10);
        assertThat(jsonModel.<List<Integer>>get("$.registerTypes[*].id")).containsOnly(112, 113, 114, 115, 116, 117, 118, 119, 120, 121);
    }

    @Test
    public void testGetDeviceConfigurationById() throws Exception {
        DeviceType deviceType = mockDeviceType("getUnfiltered", 41);
        mockDeviceConfiguration(14, deviceType);

        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetDeviceConfigurationByIdButNoSuchConfigOnTheDevice() throws Exception {
        DeviceType deviceType = mockDeviceType("some", 41);

        doReturn(Optional.empty()).when(deviceConfigurationService).findDeviceConfiguration(14);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(14, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(14, BAD_VERSION);

        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetDeviceConfigurationByIdWithNonExistingDeviceType() throws Exception {
        long deviceType_id = 41;
        when(deviceConfigurationService.findDeviceType(deviceType_id)).thenReturn(Optional.empty());
        doReturn(Optional.empty()).when(deviceConfigurationService).findDeviceConfiguration(14);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(14, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockDeviceConfigurationByIdAndVersion(14, BAD_VERSION);

        Response response = target("/devicetypes/41/deviceconfigurations/14").request().get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllRegisterConfig() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations").request()
                .get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAllRegisterConfigById() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        long registerConfig_id = 61;

        DeviceType deviceType = mockDeviceType("Device Type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);
        NumericalRegisterSpec registerSpec = mockNumericalRegister(registerConfig_id, deviceConfiguration);

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request()
                .get(Response.class);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testCreateRegisterConfigWithoutLinkedChannelSpec() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        long registerType_id = 133;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);

        ReadingType readingType = mockReadingType();

        ObisCode obisCode = mockObisCode();

        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getReadingType()).thenReturn(readingType);
        when(masterDataService.findRegisterType(registerType_id)).thenReturn(Optional.of(registerType));

        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerSpec.getObisCode()).thenReturn(obisCode);
        when(registerSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(registerSpec.getReadingType()).thenReturn(readingType);
        when(registerSpec.getOverflowValue()).thenReturn(Optional.empty());

        NumericalRegisterSpec.Builder registerSpecBuilder = mock(NumericalRegisterSpec.Builder.class, Answers.RETURNS_SELF);
        when(deviceConfiguration.createNumericalRegisterSpec(Matchers.<RegisterType>any())).thenReturn(registerSpecBuilder);
        when(registerSpecBuilder.add()).thenReturn(registerSpec);

        RegisterConfigInfo registerConfigInfo = new RegisterConfigInfo();
        registerConfigInfo.registerType = registerType_id;
        registerConfigInfo.numberOfFractionDigits = 6;
        registerConfigInfo.overflow = BigDecimal.TEN;
        registerConfigInfo.overruledObisCode = null;

        Entity<RegisterConfigInfo> json = Entity.json(registerConfigInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/").request()
                .post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        ArgumentCaptor<RegisterType> registerTypeArgumentCaptor = ArgumentCaptor.forClass(RegisterType.class);
        verify(registerSpecBuilder).numberOfFractionDigits(6);
        verify(registerSpecBuilder).overflowValue(BigDecimal.TEN);
        verify(registerSpecBuilder).noMultiplier();
        verify(deviceConfiguration).createNumericalRegisterSpec(registerTypeArgumentCaptor.capture());
        assertThat(registerTypeArgumentCaptor.getValue()).isEqualTo(registerType);
    }

    @Test
    public void testUpdateRegisterConfigWithoutLinkedChannelSpec() throws Exception {
        long deviceType_id = 41;
        long deviceConfig_id = 51;
        long registerSpec_id = 61;
        long registerType_id = 133;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);

        ReadingType readingType = mockReadingType();

        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getReadingType()).thenReturn(readingType);
        when(masterDataService.findRegisterType(registerType_id)).thenReturn(Optional.of(registerType));

        ObisCode obisCode = mockObisCode();

        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerSpec.getId()).thenReturn(registerSpec_id);
        when(registerSpec.getObisCode()).thenReturn(obisCode);
        when(registerSpec.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(registerSpec.getVersion()).thenReturn(OK_VERSION);
        when(registerSpec.getReadingType()).thenReturn(readingType);
        when(registerSpec.getOverflowValue()).thenReturn(Optional.empty());
        deviceConfiguration.getRegisterSpecs().add(registerSpec);

        NumericalRegisterSpec.Builder registerSpecBuilder = mock(NumericalRegisterSpec.Builder.class, Answers.RETURNS_SELF);
        when(registerSpecBuilder.add()).thenReturn(registerSpec);
        NumericalRegisterSpec.Updater updater = mock(NumericalRegisterSpec.Updater.class);
        when(deviceConfiguration.getRegisterSpecUpdaterFor(registerSpec)).thenReturn(updater);

        RegisterConfigInfo registerConfigInfo = registerConfigInfoFactory.asInfo(registerSpec, Collections.emptyList());
        registerConfigInfo.registerType = registerType_id;
        registerConfigInfo.numberOfFractionDigits = 6;
        registerConfigInfo.overflow = BigDecimal.valueOf(123);
        registerConfigInfo.overruledObisCode = obisCode;

        doReturn(Optional.of(registerSpec)).when(deviceConfigurationService).findRegisterSpec(registerSpec_id);
        doReturn(Optional.of(registerSpec)).when(deviceConfigurationService)
                .findAndLockRegisterSpecByIdAndVersion(registerSpec_id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockRegisterSpecByIdAndVersion(registerSpec_id, BAD_VERSION);

        Entity<RegisterConfigInfo> json = Entity.json(registerConfigInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request()
                .put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<ObisCode> obisCodeArgumentCaptor = ArgumentCaptor.forClass(ObisCode.class);
        verify(updater).overruledObisCode(obisCodeArgumentCaptor.capture());
        assertThat(obisCodeArgumentCaptor.getValue().toString()).isEqualTo(obisCode.toString());
        verify(updater).overflowValue(BigDecimal.valueOf(123));
        verify(updater).numberOfFractionDigits(6);
        verify(updater).noMultiplier();
        verify(deviceConfiguration).getRegisterSpecUpdaterFor(registerSpec);
        verify(updater).update();
    }

    @Test
    public void testGetAllConnectionMethodJavaScriptMappings() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long connectionMethodId = 61L;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);

        PartialScheduledConnectionTask partialConnectionTask = mockPartialScheduledConnectionTask(connectionMethodId, deviceConfiguration);

        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.<PartialConnectionTask>asList(partialConnectionTask));
        PropertyInfo propertyInfo = new PropertyInfo("key", "key", new PropertyValueInfo<>("value", null, null, true), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Map<String, Object> response = target("/devicetypes/41/deviceconfigurations/51/connectionmethods").request()
                .get(Map.class);
        assertThat(response).hasSize(2);
        assertThat(response.get("total")).isEqualTo(1);
        List<Map<String, Object>> connectionMethods = (List<Map<String, Object>>) response.get("data");
        assertThat(connectionMethods).hasSize(1);
        Map<String, Object> connectionMethod = connectionMethods.get(0);
        assertThat(connectionMethod).hasSize(16)
                .containsKey("id")
                .containsKey("name")
                .containsKey("direction")
                .containsKey("connectionTypePluggableClass")
                .containsKey("comWindowStart")
                .containsKey("comWindowEnd")
                .containsKey("isDefault")
                .containsKey("numberOfSimultaneousConnections")
                .containsKey("rescheduleRetryDelay")
                .containsKey("connectionStrategyInfo")
                .containsKey("properties")
                .containsKey("temporalExpression")
                .containsKey("version")
                .containsKey("protocolDialectConfigurationProperties")
                .containsKey("parent");
        List<Map<String, Object>> propertyInfos = (List<Map<String, Object>>) connectionMethod.get("properties");
        assertThat(propertyInfos).isNotNull().hasSize(1);
        Map<String, Object> macAddressProperty = propertyInfos.get(0);
        assertThat(macAddressProperty)
                .hasSize(5)
                .containsKey("key")
                .containsKey("name")
                .containsKey("propertyValueInfo")
                .containsKey("propertyTypeInfo")
                .containsKey("required");
    }

    private PartialScheduledConnectionTask mockPartialScheduledConnectionTask(long id, DeviceConfiguration deviceConfiguration) {
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getName()).thenReturn("connection type PC");

        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn("macAddress");
        when(propertySpec1.getDisplayName()).thenReturn("MAC address");
        when(propertySpec1.getValueFactory()).thenReturn(new StringFactory());
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("macAddress", "aa:bb:cc:dd:ee:ff");

        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getPropertySpecs()).thenReturn(Arrays.<PropertySpec>asList(propertySpec1));

        NextExecutionSpecs nextExecSpecs = mock(NextExecutionSpecs.class);
        when(nextExecSpecs.getTemporalExpression()).thenReturn(new TemporalExpression(TimeDuration.minutes(60)));

        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getConnectionType()).thenReturn(connectionType);
        when(partialConnectionTask.getId()).thenReturn(id);
        when(partialConnectionTask.getName()).thenReturn("connection method");
        when(partialConnectionTask.getCommunicationWindow()).thenReturn(new ComWindow(100, 200));
        when(partialConnectionTask.getNumberOfSimultaneousConnections()).thenReturn(2);
        when(partialConnectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(partialConnectionTask.getRescheduleDelay()).thenReturn(TimeDuration.minutes(15));
        when(partialConnectionTask.getProperties()).thenReturn(Collections.emptyList());
        when(partialConnectionTask.getNextExecutionSpecs()).thenReturn(nextExecSpecs);
        when(partialConnectionTask.getTypedProperties()).thenReturn(typedProperties);
        when(partialConnectionTask.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        when(partialConnectionTask.getVersion()).thenReturn(OK_VERSION);
        when(partialConnectionTask.getConfiguration()).thenReturn(deviceConfiguration);
        deviceConfiguration.getPartialConnectionTasks().add(partialConnectionTask);

        doReturn(Optional.of(partialConnectionTask)).when(deviceConfigurationService).findPartialConnectionTask(id);
        doReturn(Optional.of(partialConnectionTask)).when(deviceConfigurationService)
                .findAndLockPartialConnectionTaskByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockPartialConnectionTaskByIdAndVersion(id, BAD_VERSION);
        return partialConnectionTask;
    }

    @Test
    public void testUpdateConnectionMethodNormalProperties() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long connectionMethodId = 71L;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);
        PartialScheduledConnectionTask partialScheduledConnectionTask = mockPartialScheduledConnectionTask(connectionMethodId, deviceConfiguration);

        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClassByName("ConnType")).thenReturn(Optional.of(connectionTypePluggableClass));

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getId()).thenReturn(1234L);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialectName()).thenReturn("Dialectje");
        when(deviceConfigurationService.getProtocolDialectConfigurationProperties(1234L)).thenReturn(Optional.of(protocolDialectConfigurationProperties));

        ScheduledConnectionMethodInfo connectionMethodInfo = new ScheduledConnectionMethodInfo();
        connectionMethodInfo.name = "connection method";
        connectionMethodInfo.id = connectionMethodId;
        connectionMethodInfo.comWindowStart = 3600;
        connectionMethodInfo.comWindowEnd = 7200;
        connectionMethodInfo.isDefault = true;
        connectionMethodInfo.numberOfSimultaneousConnections = 2;
        connectionMethodInfo.connectionTypePluggableClass = "ConnType";
        connectionMethodInfo.version = OK_VERSION;
        connectionMethodInfo.parent = new VersionInfo<>(deviceConfig_id, OK_VERSION);
        connectionMethodInfo.protocolDialectConfigurationProperties = new ConnectionMethodInfo.ProtocolDialectConfigurationPropertiesInfo();
        connectionMethodInfo.protocolDialectConfigurationProperties.id = 1234L;
        connectionMethodInfo.protocolDialectConfigurationProperties.name = "Dialectje";

        Entity<ScheduledConnectionMethodInfo> json = Entity.json(connectionMethodInfo);
        PropertyInfo propertyInfo = new PropertyInfo("key", "key", new PropertyValueInfo<>("value", null, null, true), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/connectionmethods/71").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDeleteRegisterConfig() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long registerSpec_id = 61L;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);
        NumericalRegisterSpec registerSpec = mockNumericalRegister(registerSpec_id, deviceConfiguration);
        RegisterConfigInfo info = registerConfigInfoFactory.asInfo(registerSpec, Collections.emptyList());

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(deviceConfiguration).deleteRegisterSpec(registerSpec);
    }

    @Test
    public void testDeleteRegisterConfigBadVersion() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long registerSpec_id = 61L;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);
        NumericalRegisterSpec registerSpec = mockNumericalRegister(registerSpec_id, deviceConfiguration);
        RegisterConfigInfo info = registerConfigInfoFactory.asInfo(registerSpec, Collections.emptyList());
        info.version = BAD_VERSION;

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteRegisterConfigBadParentVersion() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        long registerSpec_id = 61L;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceConfig_id, deviceType);
        NumericalRegisterSpec registerSpec = mockNumericalRegister(registerSpec_id, deviceConfiguration);
        RegisterConfigInfo info = registerConfigInfoFactory.asInfo(registerSpec, Collections.emptyList());
        info.parent.version = BAD_VERSION;

        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/61").request()
                .build(HttpMethod.DELETE, Entity.json(info))
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testCreateRegisterConfigWithLinkToNonExistingRegisterType() throws Exception {
        long deviceType_id = 41L;
        long deviceConfig_id = 51L;
        DeviceType deviceType = mockDeviceType("Device type", deviceType_id);
        mockDeviceConfiguration(deviceConfig_id, deviceType);

        RegisterConfigInfo registerConfigInfo = new RegisterConfigInfo();
        when(masterDataService.findRegisterType(12345)).thenReturn(Optional.empty());
        registerConfigInfo.registerType = 12345L;
        Entity<RegisterConfigInfo> json = Entity.json(registerConfigInfo);
        Response response = target("/devicetypes/41/deviceconfigurations/51/registerconfigurations/").request()
                .post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testConstraintViolationResultsInProperJson() throws Exception {
        // Backend has RM 101 and 102, UI sets for 101: delete 102
        long RM_ID_1 = 101L;
        long RM_ID_2 = 102L;

        RegisterTypeInfo registerTypeInfo1 = new RegisterTypeInfo();
        registerTypeInfo1.id = RM_ID_1;
        registerTypeInfo1.obisCode = new ObisCode(1, 11, 2, 12, 3, 13);

        DeviceType deviceType = mockDeviceType("updater", 31);
        RegisterType registerType101 = mock(RegisterType.class);
        when(registerType101.getId()).thenReturn(RM_ID_1);
        RegisterType registerType102 = mock(RegisterType.class);
        when(registerType102.getId()).thenReturn(RM_ID_2);
        when(deviceType.getRegisterTypes()).thenReturn(Arrays.asList(registerType101, registerType102));
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = this.mockFinder(Collections.<DeviceProtocolPluggableClass>emptyList());
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        when(masterDataService.findRegisterType(RM_ID_1)).thenReturn(Optional.of(registerType101));
        when(masterDataService.findRegisterType(RM_ID_2)).thenReturn(Optional.of(registerType102));

        Thesaurus thesaurus = mock(Thesaurus.class);
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);
        MessageSeed messageSeed = mock(MessageSeed.class);
        doThrow(new SomeLocalizedException(thesaurus, messageSeed)).when(deviceType).update();

        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.registerTypes = Arrays.asList(registerTypeInfo1);
        deviceTypeInfo.version = OK_VERSION;
        Entity<DeviceTypeInfo> json = Entity.json(deviceTypeInfo);
        Response response = target("/devicetypes/31").request().put(json);
        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
    }

    @Test
    public void testGetAvailableConnectionMethodsForDevice() throws Exception {
        DeviceType deviceType = mockDeviceType("updater", 31);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(32, deviceType);
        Device device = mock(Device.class);
        when(deviceService.findDeviceById(13L)).thenReturn(Optional.of(device));
        ConnectionTask<?, ?> connectionTask1 = mockConnectionTask(101L);
        ConnectionTask<?, ?> connectionTask2 = mockConnectionTask(102L);
        ConnectionTask<?, ?> connectionTask3 = mockConnectionTask(103L);
        when(device.getConnectionTasks()).thenReturn(Arrays.<ConnectionTask<?, ?>>asList(connectionTask3, connectionTask1, connectionTask2));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        PartialConnectionTask partialConnectionTask1 = mockPartialScheduledConnectionTask(101L, deviceConfiguration);
        PartialConnectionTask partialConnectionTask2 = mockPartialScheduledConnectionTask(102L, deviceConfiguration);
        PartialConnectionTask partialConnectionTask3 = mockPartialScheduledConnectionTask(103L, deviceConfiguration);
        PartialConnectionTask partialConnectionTask4 = mockPartialScheduledConnectionTask(104L, deviceConfiguration);
        PropertyInfo propertyInfo = new PropertyInfo("key", "key", new PropertyValueInfo<>("value", null, null, true), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Map<String, Object> response = target("/devicetypes/31/deviceconfigurations/32/connectionmethods/").queryParam("available", "true")
                .queryParam("deviceId", "13")
                .request()
                .get(Map.class);
        assertThat(response.get("total")).isEqualTo(1);
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        assertThat(data.get(0).get("id")).isEqualTo(104);
        assertThat(response).isNotNull();

    }

    @Test
    public void testUpdateDeviceLifeCycleForDeviceTypeSuccess() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("Device Type 1");
        when(deviceType.getLoadProfileTypes()).thenReturn(Collections.emptyList());
        when(deviceType.getRegisterTypes()).thenReturn(Collections.emptyList());
        when(deviceType.getLogBookTypes()).thenReturn(Collections.emptyList());
        when(deviceType.getConfigurations()).thenReturn(Collections.emptyList());
        when(deviceType.canActAsGateway()).thenReturn(true);
        when(deviceType.isDirectlyAddressable()).thenReturn(true);
        when(deviceType.getVersion()).thenReturn(1L);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.empty());
        DeviceLifeCycle targetDeviceLifeCycle = mock(DeviceLifeCycle.class);
        when(targetDeviceLifeCycle.getId()).thenReturn(2L);
        when(targetDeviceLifeCycle.getName()).thenReturn("Device life cycle 2");
        when(deviceConfigurationService.findAndLockDeviceType(1L, 1)).thenReturn(Optional.of(deviceType));
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(2L)).thenReturn(Optional.of(targetDeviceLifeCycle));
        when(deviceType.getDeviceLifeCycle()).thenReturn(targetDeviceLifeCycle);

        ChangeDeviceLifeCycleInfo info = new ChangeDeviceLifeCycleInfo();
        info.targetDeviceLifeCycle = new DeviceLifeCycleInfo();
        info.targetDeviceLifeCycle.id = 2;
        info.version = 1;
        Entity<ChangeDeviceLifeCycleInfo> json = Entity.json(info);
        Response response = target("/devicetypes/1/devicelifecycle").request().put(json);
        assertThat(response.getStatus()).isEqualTo(200);
        verify(deviceConfigurationService).changeDeviceLifeCycle(deviceType, targetDeviceLifeCycle);
    }

    @Test
    public void testUpdateDeviceLifeCycleForDeviceTypeFail() throws Exception {
        long initialDeviceLifeCycleId = 1L;
        long targetDeviceLifeCycleId = 2L;

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("Device Type 1");
        when(deviceType.getLoadProfileTypes()).thenReturn(Collections.emptyList());
        when(deviceType.getRegisterTypes()).thenReturn(Collections.emptyList());
        when(deviceType.getLogBookTypes()).thenReturn(Collections.emptyList());
        when(deviceType.getConfigurations()).thenReturn(Collections.emptyList());
        when(deviceType.canActAsGateway()).thenReturn(true);
        when(deviceType.isDirectlyAddressable()).thenReturn(true);
        when(deviceType.getVersion()).thenReturn(1L);

        DeviceLifeCycle targetDeviceLifeCycle = mock(DeviceLifeCycle.class, RETURNS_DEEP_STUBS);
        when(targetDeviceLifeCycle.getId()).thenReturn(targetDeviceLifeCycleId);
        when(targetDeviceLifeCycle.getName()).thenReturn("Device life cycle 2");
        when(targetDeviceLifeCycle.getAuthorizedActions()).thenReturn(Collections.emptyList());

        DeviceLifeCycle oldDeviceLifeCycle = mock(DeviceLifeCycle.class, RETURNS_DEEP_STUBS);
        when(oldDeviceLifeCycle.getId()).thenReturn(initialDeviceLifeCycleId);
        when(oldDeviceLifeCycle.getName()).thenReturn("Device life cycle 1");
        when(oldDeviceLifeCycle.getAuthorizedActions()).thenReturn(Collections.emptyList());

        when(deviceConfigurationService.findAndLockDeviceType(1L, 1)).thenReturn(Optional.of(deviceType));
        when(deviceLifeCycleConfigurationService.findDeviceLifeCycle(targetDeviceLifeCycleId)).thenReturn(Optional.of(targetDeviceLifeCycle));
        when(deviceType.getDeviceLifeCycle()).thenReturn(oldDeviceLifeCycle);

        State state = mock(State.class, RETURNS_DEEP_STUBS);
        when(state.getId()).thenReturn(1L);
        when(state.getName()).thenReturn("Some state");

        IncompatibleFiniteStateMachineChangeException fsmEx = new IncompatibleFiniteStateMachineChangeException(state);
        IncompatibleDeviceLifeCycleChangeException dldEx = IncompatibleDeviceLifeCycleChangeException.wrapping(fsmEx);
        doThrow(dldEx).when(deviceConfigurationService).changeDeviceLifeCycle(deviceType, targetDeviceLifeCycle);
        ChangeDeviceLifeCycleInfo info = new ChangeDeviceLifeCycleInfo();
        info.targetDeviceLifeCycle = new DeviceLifeCycleInfo();
        info.targetDeviceLifeCycle.id = targetDeviceLifeCycleId;
        info.version = 1;
        Entity<ChangeDeviceLifeCycleInfo> json = Entity.json(info);
        Response response = target("/devicetypes/1/devicelifecycle").request().put(json);
        assertThat(response.getStatus()).isEqualTo(400);
        verify(deviceConfigurationService).changeDeviceLifeCycle(deviceType, targetDeviceLifeCycle);
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errorMessage")).isNotEmpty();
        assertThat(jsonModel.<Number>get("$.currentDeviceLifeCycle.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.currentDeviceLifeCycle.name")).isEqualTo("Device life cycle 1");
        assertThat(jsonModel.<Number>get("$.targetDeviceLifeCycle.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.targetDeviceLifeCycle.name")).isEqualTo("Device life cycle 2");
        assertThat(jsonModel.<List>get("$.notMappableStates")).isNotEmpty();
        assertThat(jsonModel.<String>get("$.notMappableStates.[0].name")).isEqualTo("Some state");
    }

    @Test
    public void testUpdateDeviceLifeCycleForDeviceTypeVersionicFail() throws Exception {
        mockDeviceType("device", 1);

        ChangeDeviceLifeCycleInfo info = new ChangeDeviceLifeCycleInfo();
        info.targetDeviceLifeCycle = new DeviceLifeCycleInfo();
        info.targetDeviceLifeCycle.id = 2;
        info.version = BAD_VERSION;
        Entity<ChangeDeviceLifeCycleInfo> json = Entity.json(info);
        Response response = target("/devicetypes/1/devicelifecycle").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testGetFiles() throws Exception {
        DeviceType deviceType = mockDeviceType("deviceType", 1);
        DeviceMessageFile file = mock(DeviceMessageFile.class);
        when(file.getId()).thenReturn(2L);
        when(file.getName()).thenReturn("fileName");
        when(file.getDeviceType()).thenReturn(deviceType);
        when(file.getCreateTime()).thenReturn(Instant.now());
        when(deviceType.isFileManagementEnabled()).thenReturn(true);
        when(deviceType.getDeviceMessageFiles()).thenReturn(Collections.singletonList(file));
        Response response = target("/devicetypes/1/files").request().get();
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("[0].name")).isEqualTo("fileName");
        assertThat(jsonModel.<Integer>get("[0].id")).isEqualTo(2);
    }

    @Test
    public void getCalendarsForDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("mRID", 1L);
        when(deviceConfigurationService.findDeviceType(1L)).thenReturn(Optional.of(deviceType));
        Calendar calendar = mockCalendar();
        AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
        when(allowedCalendar.isGhost()).thenReturn(false);
        when(allowedCalendar.getName()).thenReturn("CALENDAR_NAME");
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        when(deviceType.getAllowedCalendars()).thenReturn(Collections.singletonList(allowedCalendar));

        Response response = target("/devicetypes/1/timeofuse").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("[0].name")).isEqualTo("CALENDAR_NAME");
        assertThat(jsonModel.<Boolean>get("[0].ghost")).isEqualTo(false);
        assertThat(jsonModel.<String>get("[0].calendar.name")).isEqualTo("CALENDAR_NAME");
    }

    private Calendar mockCalendar() {
        Calendar calendar = mock(Calendar.class);
        when(calendar.getId()).thenReturn(1L);
        when(calendar.getName()).thenReturn("CALENDAR_NAME");
        when(calendar.getDescription()).thenReturn("CALENDAR_DESCRIPTION");
        when(calendar.getStartYear()).thenReturn(Year.of(2010));

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("ToU");
        when(calendar.getCategory()).thenReturn(category);
        when(calendar.getStatus()).thenReturn(Status.ACTIVE);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(2L);
        when(event.getName()).thenReturn("EVENT_NAME");
        when(event.getCode()).thenReturn(3L);
        when(calendar.getEvents()).thenReturn(Collections.singletonList(event));

        DayType dayType = mock(DayType.class);
        EventOccurrence eventOccurrence = mock(EventOccurrence.class);
        when(eventOccurrence.getEvent()).thenReturn(event);
        when(eventOccurrence.getId()).thenReturn(4L);
        when(eventOccurrence.getFrom()).thenReturn(LocalTime.MIDNIGHT);
        when(dayType.getEventOccurrences()).thenReturn(Collections.singletonList(eventOccurrence));
        when(dayType.getId()).thenReturn(5L);
        when(dayType.getName()).thenReturn("DAY_TYPE_NAME");

        ArrayList<DayType> dayTypes = new ArrayList<>();
        dayTypes.add(dayType);

        when(calendar.getDayTypes()).thenReturn(dayTypes);

        PeriodTransition periodTransition = mock(PeriodTransition.class);
        Period period = mock(Period.class);
        when(period.getName()).thenReturn("PERIOD_NAME");
        when(period.getId()).thenReturn(1L);
        when(periodTransition.getPeriod()).thenReturn(period);
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            when(period.getDayType(dayOfWeek)).thenReturn(dayType);
        }
        when(periodTransition.getOccurrence()).thenReturn(LocalDate.of(2016, 3, 7));

        List<PeriodTransition> periodTransitions = new ArrayList<>();
        periodTransitions.add(periodTransition);

        when(calendar.getTransitions()).thenReturn(periodTransitions);
        when(calendarService.findCalendar(1)).thenReturn(Optional.of(calendar));
        when(calendarService.findAllCalendars()).thenReturn(Collections.singletonList(calendar));

        return calendar;
    }

    private ConnectionTask<?, ?> mockConnectionTask(long partialConnectionTaskId) {
        ConnectionTask<?, ?> mock = mock(ConnectionTask.class);
        PartialInboundConnectionTask partialInboundConnectionTask = mockPartialConnectionTask(partialConnectionTaskId);
        Mockito.<PartialConnectionTask>when(mock.getPartialConnectionTask()).thenReturn(partialInboundConnectionTask);
        return mock;
    }

    private PartialInboundConnectionTask mockPartialConnectionTask(long id) {
        PartialInboundConnectionTask partialInboundConnectionTask = mock(PartialInboundConnectionTask.class);
        when(partialInboundConnectionTask.getId()).thenReturn(id);
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("someClass");
        when(partialInboundConnectionTask.getPluggableClass()).thenReturn(pluggableClass);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(partialInboundConnectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getPropertySpecs()).thenReturn(Collections.<PropertySpec>emptyList());

        doReturn(Optional.of(partialInboundConnectionTask)).when(deviceConfigurationService)
                .findPartialConnectionTask(id);
        doReturn(Optional.of(partialInboundConnectionTask)).when(deviceConfigurationService)
                .findAndLockPartialConnectionTaskByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(deviceConfigurationService)
                .findAndLockPartialConnectionTaskByIdAndVersion(id, BAD_VERSION);

        return partialInboundConnectionTask;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }

    private ObisCode mockObisCode() {
        ObisCode obisCode = mock(ObisCode.class);
        when(obisCode.getDescription()).thenReturn("desc");
        when(obisCode.toString()).thenReturn("1.1.1.1.1.1");
        return obisCode;
    }

    private ReadingType mockReadingType() {
        return mockReadingType("mrid");
    }

    class SomeLocalizedException extends LocalizedException {

        protected SomeLocalizedException(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }
    }
}