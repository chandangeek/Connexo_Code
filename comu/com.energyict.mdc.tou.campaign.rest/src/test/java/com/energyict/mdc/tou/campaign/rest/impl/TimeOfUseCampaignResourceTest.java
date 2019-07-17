/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignItem;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TimeOfUseCampaignResourceTest extends BaseTouTest {

    @Test
    public void testGetToUCampaigns() {
        TimeOfUseCampaignInfo timeOfUseCampaign = createCampaignInfo();
        QueryStream queryStream = FakeBuilder.initBuilderStub(Stream.of(timeOfUseCampaign), QueryStream.class);
        when(timeOfUseCampaignService.streamAllCampaigns()).thenReturn(queryStream);
        String json = target("toucampaigns").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].id")).isEqualTo(((Number) timeOfUseCampaign.id).intValue());
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].version")).isEqualTo(((Number) timeOfUseCampaign.version).intValue());
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].validationTimeout")).isEqualTo(((Number) timeOfUseCampaign.validationTimeout).intValue());
        assertThat(jsonModel.<String>get("$.touCampaigns[0].name")).isEqualTo(timeOfUseCampaign.name);
        assertThat(jsonModel.<String>get("$.touCampaigns[0].activationOption")).isEqualTo(timeOfUseCampaign.activationOption);
        assertThat(jsonModel.<String>get("$.touCampaigns[0].deviceGroup")).isEqualTo(timeOfUseCampaign.deviceGroup);
        assertThat(jsonModel.<String>get("$.touCampaigns[0].updateType")).isEqualTo(timeOfUseCampaign.updateType);
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].activationStart")).isEqualTo(((Number) timeOfUseCampaign.activationStart.toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].activationEnd")).isEqualTo(((Number) timeOfUseCampaign.activationEnd.toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].calendar.id")).isEqualTo(((Number) timeOfUseCampaign.calendar.id).intValue());
        assertThat(jsonModel.<String>get("$.touCampaigns[0].calendar.name")).isEqualTo(timeOfUseCampaign.calendar.name);
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].deviceType.id")).isEqualTo(((Number) timeOfUseCampaign.deviceType.id).intValue());
        assertThat(jsonModel.<String>get("$.touCampaigns[0].deviceType.name")).isEqualTo(timeOfUseCampaign.deviceType.name);
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].startedOn")).isEqualTo(((Number) timeOfUseCampaign.startedOn.toEpochMilli()).intValue());
        assertNull(jsonModel.<Number>get("$.touCampaigns[0].finishedOn"));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].status")).isEqualTo(timeOfUseCampaign.status);
    }

    @Test
    public void testGetToUCampaign() {
        TimeOfUseCampaign timeOfUseCampaign = createCampaignMock();
        when(timeOfUseCampaignService.getCampaign(timeOfUseCampaign.getId())).thenReturn(Optional.of(timeOfUseCampaign));
        String json = target("toucampaigns/3").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(((Number) timeOfUseCampaign.getId()).intValue());
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(((Number) timeOfUseCampaign.getVersion()).intValue());
        assertThat(jsonModel.<Number>get("$.validationTimeout")).isEqualTo(((Number) timeOfUseCampaign.getValidationTimeout()).intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(timeOfUseCampaign.getName());
        assertThat(jsonModel.<String>get("$.activationOption")).isEqualTo(timeOfUseCampaign.getActivationOption());
        assertThat(jsonModel.<String>get("$.deviceGroup")).isEqualTo(timeOfUseCampaign.getDeviceGroup());
        assertThat(jsonModel.<String>get("$.updateType")).isEqualTo(timeOfUseCampaign.getUpdateType());
        assertThat(jsonModel.<Number>get("$.activationStart")).isEqualTo(((Number) timeOfUseCampaign.getUploadPeriodStart().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.activationEnd")).isEqualTo(((Number) timeOfUseCampaign.getUploadPeriodEnd().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.calendar.id")).isEqualTo(((Number) timeOfUseCampaign.getCalendar().getId()).intValue());
        assertThat(jsonModel.<String>get("$.calendar.name")).isEqualTo(timeOfUseCampaign.getCalendar().getName());
        assertThat(jsonModel.<Number>get("$.deviceType.id")).isEqualTo(((Number) timeOfUseCampaign.getDeviceType().getId()).intValue());
        assertThat(jsonModel.<String>get("$.deviceType.name")).isEqualTo(timeOfUseCampaign.getDeviceType().getName());
        assertThat(jsonModel.<Number>get("$.startedOn")).isEqualTo(((Number) timeOfUseCampaign.getServiceCall().getCreationTime().toEpochMilli()).intValue());
        assertNull(jsonModel.<Number>get("$.finishedOn"));
        assertThat(jsonModel.<String>get("$.status")).isEqualTo(timeOfUseCampaign.getServiceCall().getState().getDefaultFormat());
    }

    @Test
    public void testGetToUCampaignDevices() {
        TimeOfUseCampaign timeOfUseCampaign = createCampaignMock();
        when(timeOfUseCampaignService.getCampaign(timeOfUseCampaign.getId())).thenReturn(Optional.of(timeOfUseCampaign));
        DeviceInCampaignInfo deviceInCampaignInfo1 = new DeviceInCampaignInfo(new IdWithNameInfo(1L, "TestDevice1"),
                "Pending", Instant.ofEpochSecond(3600), null);
        DeviceInCampaignInfo deviceInCampaignInfo2 = new DeviceInCampaignInfo(new IdWithNameInfo(2L, "TestDevice2"),
                "Pending", Instant.ofEpochSecond(3500), null);
        QueryStream queryStream = FakeBuilder.initBuilderStub(Stream.of(deviceInCampaignInfo1, deviceInCampaignInfo2), QueryStream.class);
        when(timeOfUseCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        String json = target("toucampaigns/3/devices").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List<DeviceInCampaignInfo>>get("$.devicesInCampaign").size() == 2);
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[0].device.id")).isEqualTo(((Number) deviceInCampaignInfo1.device.id).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[0].device.name")).isEqualTo(deviceInCampaignInfo1.device.name);
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[0].startedOn")).isEqualTo(((Number) deviceInCampaignInfo1.startedOn.toEpochMilli()).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[0].status")).isEqualTo(deviceInCampaignInfo1.status);
        assertNull(jsonModel.<Number>get("$.devicesInCampaign[0].finishedOn"));
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[1].device.id")).isEqualTo(((Number) deviceInCampaignInfo2.device.id).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[1].device.name")).isEqualTo(deviceInCampaignInfo2.device.name);
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[1].startedOn")).isEqualTo(((Number) deviceInCampaignInfo2.startedOn.toEpochMilli()).intValue());
        assertThat(jsonModel.<String>get("$.devicesInCampaign[1].status")).isEqualTo(deviceInCampaignInfo2.status);
        assertNull(jsonModel.<Number>get("$.devicesInCampaign[1].finishedOn"));
    }

    @Test
    public void testCreateToUCampaign() throws Exception {
        TimeOfUseCampaign timeOfUseCampaign = createCampaignMock();
        TimeOfUseCampaignBuilder fakeBuilder = FakeBuilder.initBuilderStub(timeOfUseCampaign, TimeOfUseCampaignBuilder.class);
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(50));
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        Calendar calendar = mock(Calendar.class);
        when(calendar.getId()).thenReturn(2L);
        when(calendar.getName()).thenReturn("TestCalendar");
        when(deviceConfigurationService.findDeviceType(1L)).thenReturn(Optional.ofNullable(deviceType));
        when(calendarService.findCalendar(2L)).thenReturn(Optional.ofNullable(calendar));
        when(timeOfUseCampaignService.newTouCampaignBuilder("TestCampaign", deviceType, calendar)).thenReturn(fakeBuilder);
        Response response = target("toucampaigns/create").request().post(Entity.json(createCampaignInfo()));
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(((Number) timeOfUseCampaign.getId()).intValue());
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(((Number) timeOfUseCampaign.getVersion()).intValue());
        assertThat(jsonModel.<Number>get("$.validationTimeout")).isEqualTo(((Number) timeOfUseCampaign.getValidationTimeout()).intValue());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo(timeOfUseCampaign.getName());
        assertThat(jsonModel.<String>get("$.activationOption")).isEqualTo(timeOfUseCampaign.getActivationOption());
        assertThat(jsonModel.<String>get("$.deviceGroup")).isEqualTo(timeOfUseCampaign.getDeviceGroup());
        assertThat(jsonModel.<String>get("$.updateType")).isEqualTo(timeOfUseCampaign.getUpdateType());
        assertThat(jsonModel.<Number>get("$.activationStart")).isEqualTo(((Number) timeOfUseCampaign.getUploadPeriodStart().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.activationEnd")).isEqualTo(((Number) timeOfUseCampaign.getUploadPeriodEnd().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.calendar.id")).isEqualTo(((Number) timeOfUseCampaign.getCalendar().getId()).intValue());
        assertThat(jsonModel.<String>get("$.calendar.name")).isEqualTo(timeOfUseCampaign.getCalendar().getName());
        assertThat(jsonModel.<Number>get("$.deviceType.id")).isEqualTo(((Number) timeOfUseCampaign.getDeviceType().getId()).intValue());
        assertThat(jsonModel.<String>get("$.deviceType.name")).isEqualTo(timeOfUseCampaign.getDeviceType().getName());
    }

    @Test
    public void testRetryDevice() throws Exception {
        Device device = mock(Device.class);
        TimeOfUseCampaignItem timeOfUseItem = mock(TimeOfUseCampaignItem.class);
        when(timeOfUseItem.getDevice()).thenReturn(device);
        when(device.getId()).thenReturn(1L);
        when(device.getName()).thenReturn("TestDevice");
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(serviceCall.getId()).thenReturn(1L);
        when(serviceCall.getState()).thenReturn(DefaultState.PENDING);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(500));
        when(serviceCall.getLastModificationTime()).thenReturn(Instant.ofEpochSecond(3600));
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(timeOfUseItem.getServiceCall()).thenReturn(serviceCall);
        when(serviceCallType.getName()).thenReturn("TestType");
        when(deviceService.findDeviceById(1L)).thenReturn(Optional.of(device));
        when(timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device)).thenReturn(Optional.of(timeOfUseItem));
        when(serviceCallService.lockServiceCall(1)).thenReturn(Optional.of(serviceCall));
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(timeOfUseItem), QueryStream.class);
        when(timeOfUseCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        when(timeOfUseItem.retry()).thenReturn(serviceCall);
        IdWithNameInfo idWithNameInfo = new IdWithNameInfo();
        idWithNameInfo.id = 1L;
        Response response = target("toucampaigns/retryDevice").request().put(Entity.json(idWithNameInfo));
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.device.id")).isEqualTo(((Number) device.getId()).intValue());
        assertThat(jsonModel.<String>get("$.device.name")).isEqualTo(device.getName());
        assertThat(jsonModel.<String>get("$.status")).isEqualTo(serviceCall.getState().getDefaultFormat());
        assertThat(jsonModel.<Number>get("$.startedOn")).isEqualTo(((Number) serviceCall.getCreationTime().toEpochMilli()).intValue());
        assertNull(jsonModel.<Number>get("$.finishedOn"));
    }

    @Test
    public void testCancelDevice() throws Exception {
        Device device = mock(Device.class);
        TimeOfUseCampaignItem timeOfUseItem = mock(TimeOfUseCampaignItem.class);
        when(timeOfUseItem.getDevice()).thenReturn(device);
        when(device.getId()).thenReturn(1L);
        when(device.getName()).thenReturn("TestDevice");
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(serviceCall.getId()).thenReturn(1L);
        when(serviceCall.getState()).thenReturn(DefaultState.CANCELLED);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(500));
        when(serviceCall.getLastModificationTime()).thenReturn(Instant.ofEpochSecond(3600));
        ServiceCallType serviceCallType = mock(ServiceCallType.class);
        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(timeOfUseItem.getServiceCall()).thenReturn(serviceCall);
        when(serviceCallType.getName()).thenReturn("TestType");
        when(deviceService.findDeviceById(1L)).thenReturn(Optional.of(device));
        when(timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device)).thenReturn(Optional.of(timeOfUseItem));
        when(serviceCallService.lockServiceCall(1)).thenReturn(Optional.of(serviceCall));
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(timeOfUseItem), QueryStream.class);
        when(timeOfUseCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        when(timeOfUseItem.cancel()).thenReturn(serviceCall);
        IdWithNameInfo idWithNameInfo = new IdWithNameInfo();
        idWithNameInfo.id = 1L;
        Response response = target("toucampaigns/cancelDevice").request().put(Entity.json(idWithNameInfo));
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.device.id")).isEqualTo(((Number) device.getId()).intValue());
        assertThat(jsonModel.<String>get("$.device.name")).isEqualTo(device.getName());
        assertThat(jsonModel.<String>get("$.status")).isEqualTo(serviceCall.getState().getDefaultFormat());
        assertThat(jsonModel.<Number>get("$.startedOn")).isEqualTo(((Number) serviceCall.getCreationTime().toEpochMilli()).intValue());
        assertThat(jsonModel.<Number>get("$.finishedOn")).isEqualTo(((Number) serviceCall.getLastModificationTime().toEpochMilli()).intValue());
    }

    @Test
    public void testEditCampaign() {
        when(clock.instant()).thenReturn(Instant.now());
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = createCampaignInfo();
        TimeOfUseCampaign timeOfUseCampaign = createCampaignMock();
        when(timeOfUseCampaignService.findAndLockToUCampaignByIdAndVersion(timeOfUseCampaignInfo.id, timeOfUseCampaignInfo.version))
                .thenReturn(Optional.of(timeOfUseCampaign));
        when(timeOfUseCampaignService.getCampaign(3)).thenReturn(Optional.of(timeOfUseCampaign));
        ComTask comtask = mock(ComTask.class);
        when(taskService.findComTask(anyLong())).thenReturn(Optional.of(comtask));
        when(taskService.findComTask(anyLong()).get().getName()).thenReturn("ctask");
        Response response = target("toucampaigns/3/edit").request().put(Entity.json(timeOfUseCampaignInfo));
        verify(timeOfUseCampaign).update();
    }

    @Test
    public void testCancelCampaign() {
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = createCampaignInfo();
        TimeOfUseCampaign timeOfUseCampaign = createCampaignMock();
        when(timeOfUseCampaignService.findAndLockToUCampaignByIdAndVersion(timeOfUseCampaignInfo.id, timeOfUseCampaignInfo.version))
                .thenReturn(Optional.of(timeOfUseCampaign));
        when(timeOfUseCampaignService.getCampaign(3)).thenReturn(Optional.of(timeOfUseCampaign));
        Response response = target("toucampaigns/3/cancel").request().put(Entity.json(timeOfUseCampaignInfo));
        verify(timeOfUseCampaign).cancel();
    }

    @Test
    public void testGetDeviceTypesForCalendars() {
        List<DeviceType> deviceTypes = new ArrayList<>();
        DeviceType deviceType1 = mock(DeviceType.class);
        when(deviceType1.getId()).thenReturn(1L);
        when(deviceType1.getName()).thenReturn("TestDeviceType1");
        DeviceType deviceType2 = mock(DeviceType.class);
        when(deviceType2.getId()).thenReturn(2L);
        when(deviceType2.getName()).thenReturn("TestDeviceType2");
        deviceTypes.add(deviceType1);
        deviceTypes.add(deviceType2);
        when(timeOfUseCampaignService.getDeviceTypesWithCalendars()).thenReturn(deviceTypes);
        String json = target("toucampaigns/devicetypes").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.[0].id")).isEqualTo(((Number) deviceType1.getId()).intValue());
        assertThat(jsonModel.<String>get("$.[0].name")).isEqualTo(deviceType1.getName());
        assertThat(jsonModel.<Number>get("$.[1].id")).isEqualTo(((Number) deviceType2.getId()).intValue());
        assertThat(jsonModel.<String>get("$.[1].name")).isEqualTo(deviceType2.getName());
    }

    @Test
    public void testGetComTasks() {
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        ComTaskEnablement cte = mock(ComTaskEnablement.class);

        ComTask comTask = mock(ComTask.class);
        when(comTask.getId()).thenReturn(1L);
        when(comTask.isSystemComTask()).thenReturn(true);
        when(comTask.getName()).thenReturn("comTask");

        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("devType");
        when(deviceType.getId()).thenReturn(1L);

        List<DeviceConfiguration> configsList = new ArrayList<>();
        configsList.add(deviceConfig);
        List<ComTaskEnablement> cteList = new ArrayList<>();
        cteList.add(cte);

        when(deviceConfigurationService.findDeviceType(anyLong())).thenReturn(Optional.of(deviceType));
        when(deviceType.getConfigurations()).thenReturn(configsList);
        when(deviceConfig.getComTaskEnablements()).thenReturn(cteList);
        when(cte.getComTask()).thenReturn(comTask);

        String json = target("/toucampaigns/comtasks").queryParam("type", 1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.[0].id")).isEqualTo(((Number) comTask.getId()).intValue());
        assertThat(jsonModel.<String>get("$.[0].name")).isEqualTo(comTask.getName());
    }

    @Test
    public void testGetSendOptionsForType() {
        List<DeviceType> deviceTypes = new ArrayList<>();
        DeviceType deviceType1 = mock(DeviceType.class);
        when(deviceType1.getId()).thenReturn(1L);
        when(deviceType1.getName()).thenReturn("TestDeviceType1");
        deviceTypes.add(deviceType1);
        AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
        when(allowedCalendar.isGhost()).thenReturn(false);
        when(allowedCalendar.getName()).thenReturn("TestCalendar");
        Calendar calendar = mock(Calendar.class);
        when(calendar.getId()).thenReturn(3L);
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        when(deviceType1.getAllowedCalendars()).thenReturn(Collections.singletonList(allowedCalendar));
        when(timeOfUseCampaignService.getDeviceTypesWithCalendars()).thenReturn(deviceTypes);
        TimeOfUseOptions timeOfUseOptions = mock(TimeOfUseOptions.class);
        Set<ProtocolSupportedCalendarOptions> protocolSupportedCalendarOptionsSet = new HashSet<>();
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        when(timeOfUseOptions.getOptions()).thenReturn(protocolSupportedCalendarOptionsSet);
        when(deviceConfigurationService.findDeviceType(1L)).thenReturn(Optional.of(deviceType1));
        when(deviceConfigurationService.findTimeOfUseOptions(deviceType1)).thenReturn(Optional.of(timeOfUseOptions));
        String json = target("toucampaigns/getoptions").queryParam("type", 1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.deviceType.id")).isEqualTo(((Number) deviceType1.getId()).intValue());
        assertThat(jsonModel.<String>get("$.deviceType.name")).isEqualTo(deviceType1.getName());
        assertThat(jsonModel.<Number>get("$.calendars[0].id")).isEqualTo(((Number) calendar.getId()).intValue());
        assertThat(jsonModel.<String>get("$.calendars[0].name")).isEqualTo(allowedCalendar.getName());
        assertTrue(jsonModel.<Boolean>get("$.withActivationDate"));
        assertTrue(jsonModel.<Boolean>get("$.fullCalendar"));
        assertTrue(jsonModel.<Boolean>get("$.specialDays"));
    }

    private TimeOfUseCampaignInfo createCampaignInfo() {
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = new TimeOfUseCampaignInfo();
        timeOfUseCampaignInfo.id = 3L;
        timeOfUseCampaignInfo.version = 4L;
        timeOfUseCampaignInfo.validationTimeout = 120L;
        timeOfUseCampaignInfo.name = "TestCampaign";
        timeOfUseCampaignInfo.activationOption = "immediately";
        timeOfUseCampaignInfo.deviceGroup = "TestGroup";
        timeOfUseCampaignInfo.updateType = "fullCalendar";
        timeOfUseCampaignInfo.activationStart = Instant.ofEpochSecond(100);
        timeOfUseCampaignInfo.activationEnd = Instant.ofEpochSecond(200);
        timeOfUseCampaignInfo.activationDate = Instant.ofEpochSecond(100);
        timeOfUseCampaignInfo.calendar = new IdWithNameInfo(2L, "TestCalendar");
        timeOfUseCampaignInfo.deviceType = new IdWithNameInfo(1L, "TestDeviceType");
        timeOfUseCampaignInfo.startedOn = Instant.ofEpochSecond(111);
        timeOfUseCampaignInfo.finishedOn = null;
        timeOfUseCampaignInfo.status = "Ongoing";
        timeOfUseCampaignInfo.sendCalendarComTask = new IdWithNameInfo(1L, "ctask");
        timeOfUseCampaignInfo.sendCalendarConnectionStrategy = new IdWithNameInfo(2L, "As soon as possible");
        timeOfUseCampaignInfo.validationComTask = new IdWithNameInfo(1L, "ctask");
        timeOfUseCampaignInfo.validationConnectionStrategy = new IdWithNameInfo(1L, "Minimize connections");
        return timeOfUseCampaignInfo;
    }

    private TimeOfUseCampaign createCampaignMock() {
        TimeOfUseCampaign timeOfUseCampaign = mock(TimeOfUseCampaign.class);
        ServiceCall serviceCall = mock(ServiceCall.class);
        when(timeOfUseCampaign.getServiceCall()).thenReturn(serviceCall);
        when(serviceCall.getCreationTime()).thenReturn(Instant.ofEpochSecond(111));
        when(serviceCall.getState()).thenReturn(DefaultState.ONGOING);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        Calendar calendar = mock(Calendar.class);
        when(calendar.getId()).thenReturn(2L);
        when(calendar.getName()).thenReturn("TestCalendar");
        when(timeOfUseCampaign.getName()).thenReturn("TestCampaign");
        when(timeOfUseCampaign.getDeviceType()).thenReturn(deviceType);
        when(timeOfUseCampaign.getDeviceGroup()).thenReturn("TestGroup");
        when(timeOfUseCampaign.getUploadPeriodStart()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getUploadPeriodEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(timeOfUseCampaign.getCalendar()).thenReturn(calendar);
        when(timeOfUseCampaign.getUpdateType()).thenReturn("fullCalendar");
        when(timeOfUseCampaign.getActivationOption()).thenReturn("immediately");
        when(timeOfUseCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getValidationTimeout()).thenReturn(120L);
        when(timeOfUseCampaign.getId()).thenReturn(3L);
        when(timeOfUseCampaign.getVersion()).thenReturn(4L);
        when(timeOfUseCampaign.getCalendarUploadComTaskId()).thenReturn(1L);
        when(timeOfUseCampaign.getValidationComTaskId()).thenReturn(1L);
        when(timeOfUseCampaign.getCalendarUploadConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(timeOfUseCampaign.getValidationConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        ComTask comtask = mock(ComTask.class);
        when(timeOfUseCampaignService.getComTaskById(anyLong())).thenReturn(comtask);
        when(timeOfUseCampaignService.getComTaskById(anyLong()).getName()).thenReturn("ctask");
        return timeOfUseCampaign;
    }
}
