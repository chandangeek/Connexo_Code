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
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignBuilder;
import com.energyict.mdc.tou.campaign.TimeOfUseItem;
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
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].id").equals(timeOfUseCampaign.id));
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].version").equals(timeOfUseCampaign.version));
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].validationTimeout").equals(timeOfUseCampaign.validationTimeout));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].name").equals(timeOfUseCampaign.name));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].activationOption").equals(timeOfUseCampaign.activationOption));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].deviceGroup").equals(timeOfUseCampaign.deviceGroup));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].updateType").equals(timeOfUseCampaign.updateType));
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].activationStart").equals(timeOfUseCampaign.activationStart.toEpochMilli()));
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].activationEnd").equals(timeOfUseCampaign.activationEnd.toEpochMilli()));
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].calendar.id").equals(timeOfUseCampaign.calendar.id));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].calendar.name").equals(timeOfUseCampaign.calendar.name));
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].deviceType.id").equals(timeOfUseCampaign.deviceType.id));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].deviceType.name").equals(timeOfUseCampaign.deviceType.name));
        assertThat(jsonModel.<Number>get("$.touCampaigns[0].startedOn").equals(timeOfUseCampaign.startedOn.toEpochMilli()));
        assertNull(jsonModel.<Number>get("$.touCampaigns[0].finishedOn"));
        assertThat(jsonModel.<String>get("$.touCampaigns[0].status").equals(timeOfUseCampaign.status));
    }

    @Test
    public void testGetToUCampaign() {
        TimeOfUseCampaign timeOfUseCampaign = createMockCampaign();
        when(timeOfUseCampaignService.getCampaign(timeOfUseCampaign.getId())).thenReturn(Optional.of(timeOfUseCampaign));
        String json = target("toucampaigns/3").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id").equals(timeOfUseCampaign.getId()));
        assertThat(jsonModel.<Number>get("$.version").equals(timeOfUseCampaign.getVersion()));
        assertThat(jsonModel.<Number>get("$.validationTimeout").equals(timeOfUseCampaign.getValidationTimeout()));
        assertThat(jsonModel.<String>get("$.name").equals(timeOfUseCampaign.getName()));
        assertThat(jsonModel.<String>get("$.activationOption").equals(timeOfUseCampaign.getActivationOption()));
        assertThat(jsonModel.<String>get("$.deviceGroup").equals(timeOfUseCampaign.getDeviceGroup()));
        assertThat(jsonModel.<String>get("$.updateType").equals(timeOfUseCampaign.getUpdateType()));
        assertThat(jsonModel.<Number>get("$.activationStart").equals(timeOfUseCampaign.getActivationStart().toEpochMilli()));
        assertThat(jsonModel.<Number>get("$.activationEnd").equals(timeOfUseCampaign.getActivationEnd().toEpochMilli()));
        assertThat(jsonModel.<Number>get("$.calendar.id").equals(timeOfUseCampaign.getCalendar().getId()));
        assertThat(jsonModel.<String>get("$.calendar.name").equals(timeOfUseCampaign.getCalendar().getName()));
        assertThat(jsonModel.<Number>get("$.deviceType.id").equals(timeOfUseCampaign.getDeviceType().getId()));
        assertThat(jsonModel.<String>get("$.deviceType.name").equals(timeOfUseCampaign.getDeviceType().getName()));
        assertThat(jsonModel.<Number>get("$.startedOn").equals(timeOfUseCampaign.getServiceCall().getCreationTime().toEpochMilli()));
        assertNull(jsonModel.<Number>get("$.finishedOn"));
        assertThat(jsonModel.<String>get("$.status").equals(timeOfUseCampaign.getServiceCall().getState().getDefaultFormat()));
    }

    @Test
    public void testGetToUCampaignDevices() {
        TimeOfUseCampaign timeOfUseCampaign = createMockCampaign();
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
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[0].device.id").equals(deviceInCampaignInfo1.device.id));
        assertThat(jsonModel.<String>get("$.devicesInCampaign[0].device.name").equals(deviceInCampaignInfo1.device.name));
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[0].startedOn").equals(deviceInCampaignInfo1.startedOn.toEpochMilli()));
        assertThat(jsonModel.<String>get("$.devicesInCampaign[0].status").equals(deviceInCampaignInfo1.status));
        assertNull(jsonModel.<Number>get("$.devicesInCampaign[0].finishedOn"));
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[1].device.id").equals(deviceInCampaignInfo2.device.id));
        assertThat(jsonModel.<String>get("$.devicesInCampaign[1].device.name").equals(deviceInCampaignInfo2.device.name));
        assertThat(jsonModel.<Number>get("$.devicesInCampaign[1].startedOn").equals(deviceInCampaignInfo2.startedOn.toEpochMilli()));
        assertThat(jsonModel.<String>get("$.devicesInCampaign[1].status").equals(deviceInCampaignInfo2.status));
        assertNull(jsonModel.<Number>get("$.devicesInCampaign[1].finishedOn"));
    }

    @Test
    public void testCreateToUCampaign() throws Exception {
        TimeOfUseCampaign timeOfUseCampaign = createMockCampaign();
        TimeOfUseCampaignBuilder fakeBuilder = FakeBuilder.initBuilderStub(timeOfUseCampaign, TimeOfUseCampaignBuilder.class);
        when(timeOfUseCampaignService.newTouCampaignBuilder("TestCampaign", 1L, 2L)).thenReturn(fakeBuilder);
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(50));
        Response response = target("toucampaigns/create").request().post(Entity.json(createCampaignInfo()));
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.id").equals(timeOfUseCampaign.getId()));
        assertThat(jsonModel.<Number>get("$.version").equals(timeOfUseCampaign.getVersion()));
        assertThat(jsonModel.<Number>get("$.validationTimeout").equals(timeOfUseCampaign.getValidationTimeout()));
        assertThat(jsonModel.<String>get("$.name").equals(timeOfUseCampaign.getName()));
        assertThat(jsonModel.<String>get("$.activationOption").equals(timeOfUseCampaign.getActivationOption()));
        assertThat(jsonModel.<String>get("$.deviceGroup").equals(timeOfUseCampaign.getDeviceGroup()));
        assertThat(jsonModel.<String>get("$.updateType").equals(timeOfUseCampaign.getUpdateType()));
        assertThat(jsonModel.<Number>get("$.activationStart").equals(timeOfUseCampaign.getActivationStart().toEpochMilli()));
        assertThat(jsonModel.<Number>get("$.activationEnd").equals(timeOfUseCampaign.getActivationEnd().toEpochMilli()));
        assertThat(jsonModel.<Number>get("$.calendar.id").equals(timeOfUseCampaign.getCalendar().getId()));
        assertThat(jsonModel.<String>get("$.calendar.name").equals(timeOfUseCampaign.getCalendar().getName()));
        assertThat(jsonModel.<Number>get("$.deviceType.id").equals(timeOfUseCampaign.getDeviceType().getId()));
        assertThat(jsonModel.<String>get("$.deviceType.name").equals(timeOfUseCampaign.getDeviceType().getName()));
    }

    @Test
    public void testRetryDevice() throws Exception {
        Device device = mock(Device.class);
        TimeOfUseItem timeOfUseItem = mock(TimeOfUseItem.class);
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
        when(serviceCallType.getName()).thenReturn("TestType");
        when(deviceService.findDeviceById(1L)).thenReturn(Optional.of(device));
        when(timeOfUseCampaignService.findActiveServiceCallByDevice(device)).thenReturn(Optional.of(serviceCall));
        when(serviceCallService.lockServiceCall(1)).thenReturn(Optional.of(serviceCall));
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(timeOfUseItem), QueryStream.class);
        when(timeOfUseCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        when(timeOfUseItem.retry()).thenReturn(serviceCall);
        IdWithNameInfo idWithNameInfo = new IdWithNameInfo();
        idWithNameInfo.id = 1L;
        Response response = target("toucampaigns/retryDevice").request().put(Entity.json(idWithNameInfo));
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.device.id").equals(device.getId()));
        assertThat(jsonModel.<String>get("$.device.name").equals(device.getName()));
        assertThat(jsonModel.<String>get("$.status").equals(serviceCall.getState().getDefaultFormat()));
        assertThat(jsonModel.<Number>get("$.startedOn").equals(serviceCall.getCreationTime().toEpochMilli()));
        assertNull(jsonModel.<Number>get("$.finishedOn"));
    }

    @Test
    public void testCancelDevice() throws Exception {
        Device device = mock(Device.class);
        TimeOfUseItem timeOfUseItem = mock(TimeOfUseItem.class);
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
        when(serviceCallType.getName()).thenReturn("TestType");
        when(deviceService.findDeviceById(1L)).thenReturn(Optional.of(device));
        when(timeOfUseCampaignService.findActiveServiceCallByDevice(device)).thenReturn(Optional.of(serviceCall));
        when(serviceCallService.lockServiceCall(1)).thenReturn(Optional.of(serviceCall));
        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(timeOfUseItem), QueryStream.class);
        when(timeOfUseCampaignService.streamDevicesInCampaigns()).thenReturn(queryStream);
        when(timeOfUseItem.cancel()).thenReturn(serviceCall);
        IdWithNameInfo idWithNameInfo = new IdWithNameInfo();
        idWithNameInfo.id = 1L;
        Response response = target("toucampaigns/cancelDevice").request().put(Entity.json(idWithNameInfo));
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Number>get("$.device.id").equals(device.getId()));
        assertThat(jsonModel.<String>get("$.device.name").equals(device.getName()));
        assertThat(jsonModel.<String>get("$.status").equals(serviceCall.getState().getDefaultFormat()));
        assertThat(jsonModel.<Number>get("$.startedOn").equals(serviceCall.getCreationTime().toEpochMilli()));
        assertThat(jsonModel.<Number>get("$.finishedOn").equals(serviceCall.getLastModificationTime().toEpochMilli()));
    }

    @Test
    public void testEditCampaign() {
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = createCampaignInfo();
        TimeOfUseCampaign timeOfUseCampaign = createMockCampaign();
        when(timeOfUseCampaignService.findAndLockToUCampaignByIdAndVersion(timeOfUseCampaignInfo.id, timeOfUseCampaignInfo.version))
                .thenReturn(Optional.of(timeOfUseCampaign));
        when(timeOfUseCampaignService.getCampaign(3)).thenReturn(Optional.of(timeOfUseCampaign));
        Response response = target("toucampaigns/3/edit").request().put(Entity.json(timeOfUseCampaignInfo));
        verify(timeOfUseCampaign).edit(timeOfUseCampaignInfo.name, timeOfUseCampaignInfo.activationStart, timeOfUseCampaignInfo.activationEnd);
    }

    @Test
    public void testCancelCampaign() {
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = createCampaignInfo();
        TimeOfUseCampaign timeOfUseCampaign = createMockCampaign();
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
        assertThat(jsonModel.<Number>get("$.[0].id").equals(deviceType1.getId()));
        assertThat(jsonModel.<String>get("$.[0].name").equals(deviceType1.getName()));
        assertThat(jsonModel.<Number>get("$.[1].id").equals(deviceType2.getId()));
        assertThat(jsonModel.<String>get("$.[1].name").equals(deviceType2.getName()));
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
        when(deviceConfigurationService.findTimeOfUseOptions(deviceType1)).thenReturn(Optional.of(timeOfUseOptions));
        String json = target("toucampaigns/getoptions").queryParam("type", 1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.deviceType.id").equals(deviceType1.getId()));
        assertThat(jsonModel.<String>get("$.deviceType.name").equals(deviceType1.getName()));
        assertThat(jsonModel.<Number>get("$.calendars[0].id").equals(calendar.getId()));
        assertThat(jsonModel.<String>get("$.calendars[0].name").equals(allowedCalendar.getName()));
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
        return timeOfUseCampaignInfo;
    }

    private TimeOfUseCampaign createMockCampaign() {
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
        when(timeOfUseCampaign.getActivationStart()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getActivationEnd()).thenReturn(Instant.ofEpochSecond(200));
        when(timeOfUseCampaign.getCalendar()).thenReturn(calendar);
        when(timeOfUseCampaign.getUpdateType()).thenReturn("fullCalendar");
        when(timeOfUseCampaign.getActivationOption()).thenReturn("immediately");
        when(timeOfUseCampaign.getActivationDate()).thenReturn(Instant.ofEpochSecond(100));
        when(timeOfUseCampaign.getValidationTimeout()).thenReturn(120L);
        when(timeOfUseCampaign.getId()).thenReturn(3L);
        when(timeOfUseCampaign.getVersion()).thenReturn(4L);
        return timeOfUseCampaign;
    }
}
