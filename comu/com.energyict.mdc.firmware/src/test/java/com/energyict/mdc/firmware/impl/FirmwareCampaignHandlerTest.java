/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.tasks.TaskService;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.osgi.service.event.EventConstants;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareCampaignHandlerTest {

    @Mock
    private FirmwareServiceImpl firmwareService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private Clock clock;
    @Mock
    private EventService eventService;
    @Mock
    private TaskService taskService;
    @Mock
    private JsonService jsonService;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DataModel dataModel;
    @Mock
    private DeviceInFirmwareCampaignImpl mockedDeviceInFirmwareCampaignImpl;

    private final long deviceTypeId = 485L;

    @Before
    public void setup() {
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getId()).thenReturn(deviceTypeId);
        when(firmwareService.getDataModel()).thenReturn(dataModel);
        when(dataModel.getInstance(DeviceInFirmwareCampaignImpl.class)).thenReturn(mockedDeviceInFirmwareCampaignImpl);
    }

    @Test
    public void createCampaignWithDevicesFromOtherCampaignDoesntAddSameDevicesTest() throws JsonProcessingException {
        long firmwareCampaignId = 12L;
        long deviceGroupId = 64L;

        Device device1 = getMockedDevice(1L);
        Device device2 = getMockedDevice(2L);
        Device device3 = getMockedDevice(3L);

        DeviceInFirmwareCampaignImpl alreadyPendingDeviceInOtherFirmwareCampaign = mock(DeviceInFirmwareCampaignImpl.class);
        when(alreadyPendingDeviceInOtherFirmwareCampaign.hasNonFinalStatus()).thenReturn(true);
        when(firmwareService.getDeviceInFirmwareCampaignsFor(device2)).thenReturn(Collections.singletonList(alreadyPendingDeviceInOtherFirmwareCampaign));

        getMockedEndDeviceGroup(deviceGroupId, device1, device2, device3);
        getMockedFirmwareCampaign(firmwareCampaignId);
        Map<String, Object> message = getMockedCreateCampaignMessage(firmwareCampaignId, deviceGroupId);

        when(jsonService.deserialize((byte[]) any(), any())).thenReturn(message);

        FirmwareCampaignHandler firmwareCampaignHandler = getTestInstance();
        firmwareCampaignHandler.process(getMockedMessage());
        verify(eventService, times(2)).postEvent(EventType.DEVICE_IN_FIRMWARE_CAMPAIGN_CREATED.topic(), mockedDeviceInFirmwareCampaignImpl);
    }

    private Device getMockedDevice(long deviceId) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(deviceId);
        when(device.getDeviceType()).thenReturn(deviceType);
        State activeState = mock(State.class);
        when(activeState.getName()).thenReturn("CertainlyNotDecomissioned");
        when(device.getState()).thenReturn(activeState);
        return device;
    }

    private EndDeviceGroup getMockedEndDeviceGroup(long deviceGroupId, Device... devicesWhichShouldBeInGroup) {
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(meteringGroupsService.findEndDeviceGroup(deviceGroupId)).thenReturn(Optional.of(endDeviceGroup));
        List<EndDevice> endDevices = new ArrayList<>(devicesWhichShouldBeInGroup.length);
        Stream.of(devicesWhichShouldBeInGroup).forEach(device -> {
            long deviceId = device.getId();
            EndDevice endDevice = mock(EndDevice.class);
            when(endDevice.getAmrId()).thenReturn(String.valueOf(deviceId));
            when(deviceService.findDeviceById(deviceId)).thenReturn(Optional.of(device));
            endDevices.add(endDevice);
        });
        when(endDeviceGroup.getMembers(any(Instant.class))).thenReturn(endDevices);
        return endDeviceGroup;
    }

    private FirmwareCampaign getMockedFirmwareCampaign(long firmwareCampaignId) {
        FirmwareCampaignImpl firmwareCampaign = mock(FirmwareCampaignImpl.class);
        when(firmwareService.getFirmwareCampaignById(firmwareCampaignId)).thenReturn(Optional.of(firmwareCampaign));
        when(firmwareCampaign.getDeviceType()).thenReturn(deviceType);
        return firmwareCampaign;
    }

    private Map<String, Object> getMockedCreateCampaignMessage(Long firmwareCampaignId, Long deviceGroupId) {
        Map<String, Object> message = new HashMap<>(3);
        message.put(EventConstants.EVENT_TOPIC, EventType.FIRMWARE_CAMPAIGN_CREATED.topic());
        message.put("id", firmwareCampaignId);
        message.put("deviceGroupId", deviceGroupId);
        return message;
    }

    private Message getMockedMessage() {
        return mock(Message.class);
    }

    private FirmwareCampaignHandler getTestInstance() {
        return new FirmwareCampaignHandler(jsonService, getContext());
    }

    private FirmwareCampaignHandlerContext getContext() {
        return new FirmwareCampaignHandlerContext(firmwareService, meteringGroupsService, deviceService, clock, eventService, taskService);
    }

}