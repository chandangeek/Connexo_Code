/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.FirmwareManagementDeviceUtilsImpl;


import com.jayway.jsonpath.JsonModel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class DeviceFirmwareVersionResourceTest extends BaseFirmwareTest {
    private static final String METER_VERSION = "FWC.12-SNAPSHOT";

    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ActivatedFirmwareVersion activatedMeterFirmwareVersion;
    @Mock
    private ActivatedFirmwareVersion activatedCommunicationFirmwareVersion;
    @Mock
    private FirmwareVersion meterFirmwareVersion;
    @Mock
    private FirmwareVersion communicationFirmwareVersion;
    @Mock
    private DeviceMessageCategory deviceMessageCategory;
    @Mock
    DeviceMessageService deviceMessageService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private ConnectionTaskService connectionTaskService;

    @Before
    public void setUpStubs() {
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(device.getComTaskExecutions()).thenReturn(Collections.emptyList());
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.of(activatedMeterFirmwareVersion));
        when(firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());

        when(activatedMeterFirmwareVersion.getFirmwareVersion()).thenReturn(meterFirmwareVersion);

        when(meterFirmwareVersion.getId()).thenReturn(1L);
        when(meterFirmwareVersion.getFirmwareVersion()).thenReturn(METER_VERSION);
        when(meterFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(meterFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);

        when(device.getMessages()).thenReturn(Collections.<DeviceMessage>emptyList());
        when(deviceMessageSpecificationService.getFirmwareCategory()).thenReturn(deviceMessageCategory);
        when(deviceMessageCategory.getId()).thenReturn(8);
        when(this.taskService.findFirmwareComTask()).thenReturn(Optional.<ComTask>empty());
        when(firmwareService.getFirmwareManagementDeviceUtilsFor(any(Device.class))).thenAnswer(
                invocationOnMock -> new FirmwareManagementDeviceUtilsImpl(thesaurus, deviceMessageSpecificationService, firmwareService, taskService, deviceMessageService, connectionTaskService, communicationTaskService).initFor((Device) invocationOnMock
                        .getArguments()[0], false)
        );
        when(firmwareService.getFirmwareManagementDeviceUtilsFor(any(Device.class), eq(true))).thenAnswer(
                invocationOnMock -> new FirmwareManagementDeviceUtilsImpl(thesaurus, deviceMessageSpecificationService, firmwareService, taskService, deviceMessageService, connectionTaskService, communicationTaskService).initFor((Device) invocationOnMock
                        .getArguments()[0], true)
        );

    }

    @Test
    public void testGetOnlyMeterFirmwareVersionsOnDevice() {
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(false);
        String json = target("devices/1/firmwares").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(jsonModel.<String>get("$.firmwares[0].activeVersion.firmwareVersion")).isEqualTo(METER_VERSION);
    }

    @Test
    public void getMeterAndCommunicationFirmwareVersionsOnDeviceNoCommunicationDefinedTest() {
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        String json = target("devices/1/firmwares").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.firmwares")).hasSize(2);
        assertThat(jsonModel.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(jsonModel.<String>get("$.firmwares[0].activeVersion.firmwareVersion")).isEqualTo(METER_VERSION);
        assertThat(jsonModel.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("communication");
        assertThat(jsonModel.<String>get("$.firmwares[1].activeVersion")).isNull();
    }

    @Test
    public void getMeterAndCommunicationFirmwareVersionsOnDeviceWhenCommunicationIsAlsoDefinedTest() {
        when(deviceProtocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        when(firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.of(activatedCommunicationFirmwareVersion));

        when(activatedCommunicationFirmwareVersion.getFirmwareVersion()).thenReturn(communicationFirmwareVersion);

        when(communicationFirmwareVersion.getId()).thenReturn(2L);
        String communicationVersion = "COM.321-456";
        when(communicationFirmwareVersion.getFirmwareVersion()).thenReturn(communicationVersion);
        when(communicationFirmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.FINAL);
        when(communicationFirmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);

        String json = target("/devices/1/firmwares").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.firmwares")).hasSize(2);
        assertThat(jsonModel.<String>get("$.firmwares[0].firmwareType.id")).isEqualTo("meter");
        assertThat(jsonModel.<String>get("$.firmwares[0].activeVersion.firmwareVersion")).isEqualTo(METER_VERSION);
        assertThat(jsonModel.<String>get("$.firmwares[1].firmwareType.id")).isEqualTo("communication");
        assertThat(jsonModel.<String>get("$.firmwares[1].activeVersion.firmwareVersion")).isEqualTo(communicationVersion);
    }

}
