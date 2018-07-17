/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;

import com.jayway.jsonpath.JsonModel;
import net.minidev.json.JSONArray;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComTaskEnablementResourceTest extends DeviceConfigurationApplicationJerseyTest {

    private static final long firmwareComTaskId = 103;
    private static final long loadProfilesComTaskId = 16;
    private static final long registersComTaskId = 645;
    private static final long topologyComTaskId = 46;
    private static final String topologyComTaskName = "TopologyComTask";
    private static final String registersComTaskName = "RegistersComTask";
    private static final String loadProfilesComTaskName = "LoadProfilesComTask";
    private static final String firmwareComTaskName = "Firmware management";

    DeviceType deviceType;
    DeviceConfiguration deviceConfiguration;
    ConnectionFunction connectionFunction_1, connectionFunction_2;
    ComTaskEnablement comTaskEnablement;
    MyTestComTaskEnablementBuilder comTaskEnablementBuilder;

    @Override
    protected void setupThesaurus() {
        super.setupThesaurus();
        Stream.of(TranslationKeys.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(TranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Before
    public void initBefore() {
        connectionFunction_1 = mockConnectionFunction(1, "CF_1", "CF 1");
        connectionFunction_2 = mockConnectionFunction(2, "CF_2", "CF 2");
        deviceType = mockDeviceType("device", 11);
        deviceConfiguration = mockDeviceConfiguration("config", 12);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        comTaskEnablement = mockComTaskEnablement(13L, "My task");
        when(comTaskEnablement.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        when(deviceConfigurationService.findComTaskEnablement(13L)).thenReturn(Optional.of(comTaskEnablement));
        when(firmwareService.findFirmwareManagementOptions(any(DeviceType.class))).thenReturn(Optional.empty());

        comTaskEnablementBuilder = new MyTestComTaskEnablementBuilder(comTaskEnablement);
        when(deviceConfiguration.enableComTask(any(ComTask.class), any(SecurityPropertySet.class))).thenReturn(comTaskEnablementBuilder);
    }

    @Test
    public void testGetComTaskEnablements() throws Exception {
        String response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<JSONArray>get("$.data")).hasSize(1);
    }

    @Test
    public void testGetComTaskEnablementUsingDefaultConnection() throws Exception {
        String response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements/13").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<Integer>get("$.comTask.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.comTask.name")).isEqualTo("My task");
        assertThat(jsonModel.<Boolean>get("$.ignoreNextExecutionSpecsForInbound")).isFalse();
        assertThat(jsonModel.<Integer>get("$.securityPropertySet.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.securityPropertySet.name")).isEqualTo("Security");
        assertThat(jsonModel.<Integer>get("$.priority")).isEqualTo(50);
        assertThat(jsonModel.<Boolean>get("$.suspended")).isFalse();
        assertThat(jsonModel.<Integer>get("$.partialConnectionTask.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.partialConnectionTask.name")).isEqualTo("Default");
        assertThat(jsonModel.<String>get("$.connectionFunctionInfo")).isNull();
        assertThat(jsonModel.<Integer>get("$.version")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.parent.id")).isEqualTo(12);
    }

    @Test
    public void testGetComTaskEnablementUsingSpecificConnection() throws Exception {
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(123L);
        when(partialConnectionTask.getName()).thenReturn("Partial connection");
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        when(comTaskEnablement.usesDefaultConnectionTask()).thenReturn(false);

        String response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements/13").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<Integer>get("$.comTask.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.comTask.name")).isEqualTo("My task");
        assertThat(jsonModel.<Boolean>get("$.ignoreNextExecutionSpecsForInbound")).isFalse();
        assertThat(jsonModel.<Integer>get("$.securityPropertySet.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.securityPropertySet.name")).isEqualTo("Security");
        assertThat(jsonModel.<Integer>get("$.priority")).isEqualTo(50);
        assertThat(jsonModel.<Boolean>get("$.suspended")).isFalse();
        assertThat(jsonModel.<Integer>get("$.partialConnectionTask.id")).isEqualTo(123);
        assertThat(jsonModel.<String>get("$.partialConnectionTask.name")).isEqualTo("Partial connection");
        assertThat(jsonModel.<String>get("$.connectionFunctionInfo")).isNull();
        assertThat(jsonModel.<Integer>get("$.version")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.parent.id")).isEqualTo(12);
    }

    @Test
    public void testGetComTaskEnablementUsingConnectionBasedOnConnectionFunction() throws Exception {
        Optional<ConnectionFunction> connectionFunctionOptional = Optional.of(this.connectionFunction_1);
        when(comTaskEnablement.getConnectionFunction()).thenReturn(connectionFunctionOptional);
        when(comTaskEnablement.usesDefaultConnectionTask()).thenReturn(false);
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.empty());


        String response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements/13").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<Integer>get("$.comTask.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.comTask.name")).isEqualTo("My task");
        assertThat(jsonModel.<Boolean>get("$.ignoreNextExecutionSpecsForInbound")).isFalse();
        assertThat(jsonModel.<Integer>get("$.securityPropertySet.id")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.securityPropertySet.name")).isEqualTo("Security");
        assertThat(jsonModel.<Integer>get("$.priority")).isEqualTo(50);
        assertThat(jsonModel.<Boolean>get("$.suspended")).isFalse();
        assertThat(jsonModel.<Integer>get("$.partialConnectionTask.id")).isEqualTo(-1);
        assertThat(jsonModel.<String>get("$.partialConnectionTask.name")).isEqualTo(TranslationKeys.CONNECTION_FUNCTION.getDefaultFormat());
        assertThat(jsonModel.<Integer>get("$.version")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.parent.id")).isEqualTo(12);
    }

    @Test
    public void testCreateComTaskExecutionToUSeTheDefaultConnectionTask() throws Exception {
        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        info.id = 14L;
        info.comTask = new ComTaskEnablementInfo.ComTaskInfo();
        info.comTask.id = 456L;
        info.comTask.name = "My task";
        info.connectionFunctionInfo = null;
        info.partialConnectionTask = new ComTaskEnablementInfo.PartialConnectionTaskInfo();
        info.partialConnectionTask.id = 0L;
        info.partialConnectionTask.name = "Default";
        info.connectionFunctionInfo = null;
        info.priority = 50;
        info.securityPropertySet = new ComTaskEnablementInfo.SecurityPropertySetInfo();
        info.securityPropertySet.id = 789L;
        info.securityPropertySet.name = "Security";
        info.ignoreNextExecutionSpecsForInbound = false;
        info.suspended = false;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);

        Response response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        assertThat(comTaskEnablementBuilder.getConnectionFunction()).isNull();
        assertThat(comTaskEnablementBuilder.getPartialConnectionTask()).isNull();
        assertThat(comTaskEnablementBuilder.useDefaultConnectionTask()).isTrue();
    }

    @Test
    public void testUpdateComTaskExecutionToUSeTheDefaultConnectionTask() throws Exception {
        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        info.id = 13L;
        info.comTask = new ComTaskEnablementInfo.ComTaskInfo();
        info.comTask.id = 456L;
        info.comTask.name = "My task";
        info.connectionFunctionInfo = null;
        info.partialConnectionTask = new ComTaskEnablementInfo.PartialConnectionTaskInfo();
        info.partialConnectionTask.id = 0L;
        info.partialConnectionTask.name = "Default";
        info.connectionFunctionInfo = null;
        info.priority = 50;
        info.securityPropertySet = new ComTaskEnablementInfo.SecurityPropertySetInfo();
        info.securityPropertySet.id = 789L;
        info.securityPropertySet.name = "Security";
        info.ignoreNextExecutionSpecsForInbound = false;
        info.suspended = false;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);

        Response response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<ConnectionFunction> connectionFunctionArgumentCaptor = ArgumentCaptor.forClass(ConnectionFunction.class);
        verify(comTaskEnablement, never()).setConnectionFunction(connectionFunctionArgumentCaptor.capture());

        ArgumentCaptor<Boolean> useDefaultConnectionTaskArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(comTaskEnablement).useDefaultConnectionTask(useDefaultConnectionTaskArgumentCaptor.capture());
        assertThat(useDefaultConnectionTaskArgumentCaptor.getValue()).isTrue();

        ArgumentCaptor<PartialConnectionTask> partialConnectionTaskArgumentCaptor = ArgumentCaptor.forClass(PartialConnectionTask.class);
        verify(comTaskEnablement, never()).setPartialConnectionTask(partialConnectionTaskArgumentCaptor.capture());
    }

    @Test
    public void testCreateComTaskExecutionToUSeSpecificConnectionTask() throws Exception {
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(123L);
        when(partialConnectionTask.getName()).thenReturn("Partial connection");
        doReturn(Optional.of(partialConnectionTask)).when(deviceConfigurationService).findPartialConnectionTask(anyLong());

        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        info.id = 15L;
        info.comTask = new ComTaskEnablementInfo.ComTaskInfo();
        info.comTask.id = 456L;
        info.comTask.name = "My task";
        info.connectionFunctionInfo = null;
        info.partialConnectionTask = new ComTaskEnablementInfo.PartialConnectionTaskInfo();
        info.partialConnectionTask.id = partialConnectionTask.getId();
        info.partialConnectionTask.name = partialConnectionTask.getName();
        info.connectionFunctionInfo = null;
        info.priority = 50;
        info.securityPropertySet = new ComTaskEnablementInfo.SecurityPropertySetInfo();
        info.securityPropertySet.id = 789L;
        info.securityPropertySet.name = "Security";
        info.ignoreNextExecutionSpecsForInbound = false;
        info.suspended = false;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);

        Response response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        assertThat(comTaskEnablementBuilder.getConnectionFunction()).isNull();
        assertThat(comTaskEnablementBuilder.getPartialConnectionTask()).isEqualTo(partialConnectionTask);
        assertThat(comTaskEnablementBuilder.useDefaultConnectionTask()).isFalse();
    }

    @Test
    public void testUpdateComTaskExecutionToUSeSpecificConnectionTask() throws Exception {
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(123L);
        when(partialConnectionTask.getName()).thenReturn("Partial connection");
        doReturn(Optional.of(partialConnectionTask)).when(deviceConfigurationService).findPartialConnectionTask(anyLong());

        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        info.id = 13L;
        info.comTask = new ComTaskEnablementInfo.ComTaskInfo();
        info.comTask.id = 456L;
        info.comTask.name = "My task";
        info.connectionFunctionInfo = null;
        info.partialConnectionTask = new ComTaskEnablementInfo.PartialConnectionTaskInfo();
        info.partialConnectionTask.id = partialConnectionTask.getId();
        info.partialConnectionTask.name = partialConnectionTask.getName();
        info.connectionFunctionInfo = null;
        info.priority = 50;
        info.securityPropertySet = new ComTaskEnablementInfo.SecurityPropertySetInfo();
        info.securityPropertySet.id = 789L;
        info.securityPropertySet.name = "Security";
        info.ignoreNextExecutionSpecsForInbound = false;
        info.suspended = false;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);

        Response response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<ConnectionFunction> connectionFunctionArgumentCaptor = ArgumentCaptor.forClass(ConnectionFunction.class);
        verify(comTaskEnablement, never()).setConnectionFunction(connectionFunctionArgumentCaptor.capture());

        ArgumentCaptor<Boolean> useDefaultConnectionTaskArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(comTaskEnablement, never()).useDefaultConnectionTask(useDefaultConnectionTaskArgumentCaptor.capture());

        ArgumentCaptor<PartialConnectionTask> partialConnectionTaskArgumentCaptor = ArgumentCaptor.forClass(PartialConnectionTask.class);
        verify(comTaskEnablement).setPartialConnectionTask(partialConnectionTaskArgumentCaptor.capture());
        assertThat(partialConnectionTaskArgumentCaptor.getValue()).isEqualTo(partialConnectionTask);
    }

    @Test
    public void testCreateComTaskExecutionToUSeConnectionTaskBasedOnConnectionFunction() throws Exception {
        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        info.id = 17L;
        info.comTask = new ComTaskEnablementInfo.ComTaskInfo();
        info.comTask.id = 456L;
        info.comTask.name = "My task";
        info.connectionFunctionInfo = null;
        info.partialConnectionTask = null;
        info.connectionFunctionInfo = new ConnectionFunctionInfo();
        info.connectionFunctionInfo.id = connectionFunction_2.getId();
        info.connectionFunctionInfo.localizedValue = connectionFunction_2.getConnectionFunctionDisplayName();
        info.priority = 50;
        info.securityPropertySet = new ComTaskEnablementInfo.SecurityPropertySetInfo();
        info.securityPropertySet.id = 789L;
        info.securityPropertySet.name = "Security";
        info.ignoreNextExecutionSpecsForInbound = false;
        info.suspended = false;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);

        Response response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        assertThat(comTaskEnablementBuilder.getConnectionFunction()).isEqualTo(connectionFunction_2);
        assertThat(comTaskEnablementBuilder.getPartialConnectionTask()).isNull();
        assertThat(comTaskEnablementBuilder.useDefaultConnectionTask()).isFalse();
    }

    @Test
    public void testUpdateComTaskExecutionToUSeConnectionTaskBasedOnConnectionFunction() throws Exception {
        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        info.id = 13L;
        info.comTask = new ComTaskEnablementInfo.ComTaskInfo();
        info.comTask.id = 456L;
        info.comTask.name = "My task";
        info.connectionFunctionInfo = null;
        info.partialConnectionTask = null;
        info.connectionFunctionInfo = new ConnectionFunctionInfo();
        info.connectionFunctionInfo.id = connectionFunction_2.getId();
        info.connectionFunctionInfo.localizedValue = connectionFunction_2.getConnectionFunctionDisplayName();
        info.priority = 50;
        info.securityPropertySet = new ComTaskEnablementInfo.SecurityPropertySetInfo();
        info.securityPropertySet.id = 789L;
        info.securityPropertySet.name = "Security";
        info.ignoreNextExecutionSpecsForInbound = false;
        info.suspended = false;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(12L, OK_VERSION);

        Response response = target("/devicetypes/11/deviceconfigurations/12/comtaskenablements/13").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<ConnectionFunction> connectionFunctionArgumentCaptor = ArgumentCaptor.forClass(ConnectionFunction.class);
        verify(comTaskEnablement).setConnectionFunction(connectionFunctionArgumentCaptor.capture());
        assertThat(connectionFunctionArgumentCaptor.getValue()).isEqualTo(connectionFunction_2);

        ArgumentCaptor<Boolean> useDefaultConnectionTaskArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(comTaskEnablement, never()).useDefaultConnectionTask(useDefaultConnectionTaskArgumentCaptor.capture());

        ArgumentCaptor<PartialConnectionTask> partialConnectionTaskArgumentCaptor = ArgumentCaptor.forClass(PartialConnectionTask.class);
        verify(comTaskEnablement, never()).setPartialConnectionTask(partialConnectionTaskArgumentCaptor.capture());
    }

    @Test
    public void getAllowedComTasksWhichAreNotDefinedYetNoComTasksWithFirmwareTest() {
        mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        ComTask firmwareComTask = mockFirmwareComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Collections.singletonList(firmwareComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("data")).hasSize(1);
    }

    @Test
    public void getAllowedComTasksWhichAreNotDefinedYetNoComTasksWithFirmwareButDisallowTest() {
        mockSimpleDeviceTypeAndConfig();
        ComTask firmwareComTask = mockFirmwareComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Collections.singletonList(firmwareComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).hasSize(0);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithSomeDefinedNoFirmwareDeviceTypeAllowsFirmwareTest() {
        DeviceType deviceType = mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, registersComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
        assertThat((List) map.get("data")).hasSize(2);
        assertThat(((Map) ((List) map.get("data")).get(0)).get("name")).isEqualTo(firmwareComTaskName);
        assertThat(((Map) ((List) map.get("data")).get(1)).get("name")).isEqualTo(topologyComTaskName);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithSomeDefinedNoFirmwareDeviceTypeDoesntAllowFirmwareTest() {
        DeviceType deviceType = mockSimpleDeviceTypeAndConfig();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, registersComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("data")).hasSize(1);
        assertThat(((Map) ((List) map.get("data")).get(0)).get("name")).isEqualTo(topologyComTaskName);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithSomeDefinedWithFirmwareDeviceTypeAllowFirmwareTest() {
        DeviceType deviceType = mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        ComTaskEnablement firmwareComTaskEnablement = mockFirmwareComTaskEnablement(firmwareComTask);
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, registersComTaskEnablement, firmwareComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("data")).hasSize(1);
        assertThat(((Map) ((List) map.get("data")).get(0)).get("name")).isEqualTo(topologyComTaskName);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithAllDefinedWithFirmwareDeviceTypeAllowFirmwareTest() {
        DeviceType deviceType = mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        ComTaskEnablement topologyComTaskEnablement = mockTopologyComTaskEnablement(topologyComTask);
        ComTaskEnablement firmwareComTaskEnablement = mockFirmwareComTaskEnablement(firmwareComTask);
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, topologyComTaskEnablement, registersComTaskEnablement, firmwareComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).hasSize(0);
    }

    @Test
    public void getAllowedComTaskWhichAreNotDefinedYetWithAllDefinedWithFirmwareDeviceTypeDoesntAllowFirmwareTest() {
        DeviceType deviceType = mockSimpleDeviceTypeAndConfig();
        DeviceConfiguration deviceConfiguration = deviceType.getConfigurations().get(0);
        ComTask firmwareComTask = mockFirmwareComTask();
        ComTask loadProfilesComTask = mockLoadProfilesComTask();
        ComTaskEnablement loadProfilesComTaskEnablement = mockLoadProfilesComTaskEnablement(loadProfilesComTask);
        ComTask registersComTask = mockRegistersComTask();
        ComTaskEnablement registersComTaskEnablement = mockRegistersComTaskEnablement(registersComTask);
        ComTask topologyComTask = mockTopologyComTask();
        ComTaskEnablement topologyComTaskEnablement = mockTopologyComTaskEnablement(topologyComTask);
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(firmwareComTask, loadProfilesComTask, registersComTask, topologyComTask));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(loadProfilesComTaskEnablement, topologyComTaskEnablement, registersComTaskEnablement));

        Map<String, Object> map = target("devicetypes/1/deviceconfigurations/1/comtasks").queryParam("available", true).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).hasSize(0);
    }

    private ComTaskEnablement mockTopologyComTaskEnablement(ComTask topologyComTask) {
        ComTaskEnablement topologyComTaskEnablement = mock(ComTaskEnablement.class);
        when(topologyComTaskEnablement.getComTask()).thenReturn(topologyComTask);
        return topologyComTaskEnablement;
    }

    private ComTaskEnablement mockFirmwareComTaskEnablement(ComTask firmwareComTask) {
        ComTaskEnablement firmwareComTaskEnablement = mock(ComTaskEnablement.class);
        when(firmwareComTaskEnablement.getComTask()).thenReturn(firmwareComTask);
        return firmwareComTaskEnablement;
    }

    private ComTask mockTopologyComTask() {
        ComTask topologyComTask = mock(ComTask.class);
        when(topologyComTask.getId()).thenReturn(topologyComTaskId);
        when(topologyComTask.getName()).thenReturn(topologyComTaskName);
        return topologyComTask;
    }

    private ComTaskEnablement mockRegistersComTaskEnablement(ComTask registersComTask) {
        ComTaskEnablement registersComTaskEnablement = mock(ComTaskEnablement.class);
        when(registersComTaskEnablement.getComTask()).thenReturn(registersComTask);
        return registersComTaskEnablement;
    }

    private ComTask mockRegistersComTask() {
        ComTask registersComTask = mock(ComTask.class);
        when(registersComTask.getId()).thenReturn(registersComTaskId);
        when(registersComTask.getName()).thenReturn(registersComTaskName);
        return registersComTask;
    }

    private ComTaskEnablement mockLoadProfilesComTaskEnablement(ComTask loadProfilesComTask) {
        ComTaskEnablement loadProfilesComTaskEnablement = mock(ComTaskEnablement.class);
        when(loadProfilesComTaskEnablement.getComTask()).thenReturn(loadProfilesComTask);
        return loadProfilesComTaskEnablement;
    }

    private ComTask mockLoadProfilesComTask() {
        ComTask loadProfilesComTask = mock(ComTask.class);
        when(loadProfilesComTask.getId()).thenReturn(loadProfilesComTaskId);
        when(loadProfilesComTask.getName()).thenReturn(loadProfilesComTaskName);
        return loadProfilesComTask;
    }

    private DeviceType mockDeviceTypeWithConfigWhichAllowsFirmwareUpgrade() {
        DeviceType deviceType = mockSimpleDeviceTypeAndConfig();
        FirmwareManagementOptions firmwareUpgradeOption = mock(FirmwareManagementOptions.class);
        when(firmwareService.findFirmwareManagementOptions(deviceType)).thenReturn(Optional.of(firmwareUpgradeOption));
        return deviceType;
    }

    private DeviceType mockSimpleDeviceTypeAndConfig() {
        DeviceType deviceType = mockDeviceType("device", 1);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration("config", 1, deviceType);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(1)).thenReturn(Optional.of(deviceType));

        return deviceType;
    }

    private ComTask mockFirmwareComTask() {
        ComTask firmwareComTask = mock(ComTask.class);
        when(firmwareComTask.getId()).thenReturn(firmwareComTaskId);
        when(firmwareComTask.getName()).thenReturn(firmwareComTaskName);
        FirmwareManagementTask firmwareManagementTask = mock(FirmwareManagementTask.class);
        when(firmwareComTask.getProtocolTasks()).thenReturn(Collections.singletonList(firmwareManagementTask));
        when(taskService.findFirmwareComTask()).thenReturn(Optional.of(firmwareComTask));
        return firmwareComTask;
    }

    protected DeviceType mockDeviceType(String name, long id) {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn(name);
        when(deviceType.getId()).thenReturn(id);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocolPluggableClass.getProvidedConnectionFunctions()).thenReturn(Arrays.asList(connectionFunction_1, connectionFunction_2));
        when(deviceProtocolPluggableClass.getConsumableConnectionFunctions()).thenReturn(Collections.singletonList(connectionFunction_2));
        return deviceType;
    }

    private DeviceConfiguration mockDeviceConfiguration(String name, long id, DeviceType deviceType) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);

        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService).findDeviceConfiguration(id);
        doReturn(Optional.of(deviceConfiguration)).when(deviceConfigurationService).findAndLockDeviceConfigurationByIdAndVersion(eq(id), anyLong());
        return deviceConfiguration;
    }

    private DeviceConfiguration mockDeviceConfiguration(String name, long id) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getName()).thenReturn(name);
        when(deviceConfiguration.getId()).thenReturn(id);
        when(deviceConfiguration.getVersion()).thenReturn(1L);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(101L);
        when(deviceConfiguration.getRegisterSpecs()).thenReturn(Collections.singletonList(registerSpec));
        when(deviceConfigurationService.findDeviceConfiguration(id)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return deviceConfiguration;
    }

    private ComTaskEnablement mockComTaskEnablement(long id, String name) {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getName()).thenReturn(name);
        when(taskService.findComTask(anyLong())).thenReturn(Optional.of(comTask));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getName()).thenReturn("Security");
        doReturn(Optional.of(securityPropertySet)).when(deviceConfigurationService).findSecurityPropertySet(anyLong());

        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getId()).thenReturn(id);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskEnablement.getPartialConnectionTask()).thenReturn(Optional.empty());
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comTaskEnablement.usesDefaultConnectionTask()).thenReturn(true);
        when(comTaskEnablement.getConnectionFunction()).thenReturn(Optional.empty());
        when(comTaskEnablement.getPriority()).thenReturn(50);

        doReturn(Optional.of(comTaskEnablement)).when(deviceConfigurationService).findAndLockComTaskEnablementByIdAndVersion(eq(id), anyLong());
        return comTaskEnablement;
    }

    private ConnectionFunction mockConnectionFunction(int id, String name, String displayName) {
        return new ConnectionFunction() {
            @Override
            public long getId() {
                return id;
            }

            @Override
            public String getConnectionFunctionName() {
                return name;
            }

            @Override
            public String getConnectionFunctionDisplayName() {
                return displayName;
            }
        };
    }

    private class MyTestComTaskEnablementBuilder implements ComTaskEnablementBuilder {

        PartialConnectionTask partialConnectionTask;
        boolean useDefaultConnectionTask;
        ConnectionFunction connectionFunction;
        ComTaskEnablement comTaskEnablement;

        public MyTestComTaskEnablementBuilder(ComTaskEnablement comTaskEnablement) {
            this.comTaskEnablement = comTaskEnablement;
        }

        @Override
        public ComTaskEnablementBuilder setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
            this.partialConnectionTask = partialConnectionTask;
            return this;
        }

        @Override
        public ComTaskEnablementBuilder useDefaultConnectionTask(boolean flagValue) {
            this.useDefaultConnectionTask = flagValue;
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setConnectionFunction(ConnectionFunction connectionFunction) {
            this.connectionFunction = connectionFunction;
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setIgnoreNextExecutionSpecsForInbound(boolean flag) {
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setPriority(int priority) {
            return this;
        }

        @Override
        public ComTaskEnablement add() {
            return comTaskEnablement;
        }

        public PartialConnectionTask getPartialConnectionTask() {
            return partialConnectionTask;
        }

        public boolean useDefaultConnectionTask() {
            return useDefaultConnectionTask;
        }

        public ConnectionFunction getConnectionFunction() {
            return connectionFunction;
        }
    }
}