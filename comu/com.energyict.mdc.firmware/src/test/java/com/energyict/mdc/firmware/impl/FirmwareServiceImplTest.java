/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareServiceImplTest extends PersistenceTest {

    @Test
    public void getProtocolSupportedFirmwareOptionsWhenNoneAreApplicableTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds();

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).isEmpty();
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateImmediateTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateLaterTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_BROADCAST_FIRMWARE_UPGRADE);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateDateTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithActivateLaterAndActivateImmediateTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE
        );

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithAllOptionsTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_BROADCAST_FIRMWARE_UPGRADE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE
        );

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithAllOptionsAndDuplicatesByTheProtocolTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_BROADCAST_FIRMWARE_UPGRADE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE
        );

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
    }

    @Test
    public void protocolWithNoSupportedDeviceMessagesDoesNotNeedImageIdentifierToUploadFirmwareTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds();
        assertThat(deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().getSupportedMessages()).isEmpty();
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)).isFalse();
    }

    @Test
    public void protocolDoesNotNeedImageIdentifierToUploadFirmwareTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE,
                DeviceMessageId.CONTACTOR_OPEN,
                DeviceMessageId.ALARM_CONFIGURATION_WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2,
                DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME,
                DeviceMessageId.NETWORK_CONNECTIVITY_SET_SUBNET_MASK,
                DeviceMessageId.FIRMWARE_UPGRADE_FTION_UPGRADE_RF_MESH_FIRMWARE,
                DeviceMessageId.ZIGBEE_CONFIGURATION_READ_STATUS,
                DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_USING_SERVICE_KEY,
                DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_PUSH_CONFIGURATION);
        assertThat(deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().getSupportedMessages()).hasSize(9);
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)).isFalse();
    }

    @Test
    public void protocolNeedsImageIdentifierToUploadFirmwareTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)).isTrue();
    }

    @Test
    public void protocolWithNoSupportedDeviceMessagesCannotResumeUploadFirmwareTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds();
        assertThat(deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().getSupportedMessages()).isEmpty();
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.isResumeFirmwareUploadEnabled(deviceType)).isFalse();
    }

    @Test
    public void protocolCannotResumeFirmwareUploadTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME_AND_TYPE,
                DeviceMessageId.CONTACTOR_OPEN,
                DeviceMessageId.ALARM_CONFIGURATION_WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2,
                DeviceMessageId.PLC_CONFIGURATION_SET_MAX_JOIN_WAIT_TIME,
                DeviceMessageId.NETWORK_CONNECTIVITY_SET_SUBNET_MASK,
                DeviceMessageId.FIRMWARE_UPGRADE_FTION_UPGRADE_RF_MESH_FIRMWARE,
                DeviceMessageId.ZIGBEE_CONFIGURATION_READ_STATUS,
                DeviceMessageId.SECURITY_CHANGE_HLS_SECRET_USING_SERVICE_KEY,
                DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_PUSH_CONFIGURATION);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.isResumeFirmwareUploadEnabled(deviceType)).isFalse();
    }

    @Test
    public void protocolCanResumeFirmwareUploadTest() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();

        DeviceType deviceType5002 = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE);
        assertThat(firmwareService.isResumeFirmwareUploadEnabled(deviceType5002)).isTrue();

        DeviceType deviceType5003 = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE);

        assertThat(firmwareService.isResumeFirmwareUploadEnabled(deviceType5003)).isTrue();

        DeviceType deviceType5030 = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME);

        assertThat(firmwareService.isResumeFirmwareUploadEnabled(deviceType5030)).isTrue();
    }

    private DeviceType getMockedDeviceTypeWithMessageIds(DeviceMessageId... deviceMessageIds) {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        List<DeviceMessageSpec> deviceMessageSpecs = mockMessages(deviceMessageIds);
        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageSpecs);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        return deviceType;
    }

    private List<DeviceMessageSpec> mockMessages(DeviceMessageId... deviceMessageIds) {
        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> result = new ArrayList<>();
        for (DeviceMessageId deviceMessageId : deviceMessageIds) {
            com.energyict.mdc.upl.messages.DeviceMessageSpec spec = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(spec.getId()).thenReturn(deviceMessageId.dbValue());

            result.add(spec);
        }
        return result;
    }
}
