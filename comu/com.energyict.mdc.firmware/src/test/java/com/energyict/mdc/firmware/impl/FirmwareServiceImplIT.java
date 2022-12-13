/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareServiceImplIT extends PersistenceTest {

    @Test
    @Transactional
    public void getProtocolSupportedFirmwareOptionsWhenNoneAreApplicableTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds();

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).isEmpty();
    }

    @Test
    @Transactional
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateImmediateTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
    }

    @Test
    @Transactional
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateLaterTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_BROADCAST_FIRMWARE_UPGRADE);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
    }

    @Test
    @Transactional
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateDateTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
    }

    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void protocolWithNoSupportedDeviceMessagesDoesNotNeedImageIdentifierToUploadFirmwareTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds();
        assertThat(deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().getSupportedMessages()).isEmpty();
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)).isFalse();
    }

    @Test
    @Transactional
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
    @Transactional
    public void protocolNeedsImageIdentifierToUploadFirmwareTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER);
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.imageIdentifierExpectedAtFirmwareUpload(deviceType)).isTrue();
    }

    @Test
    @Transactional
    public void protocolWithNoSupportedDeviceMessagesCannotResumeUploadFirmwareTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds();
        assertThat(deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol().getSupportedMessages()).isEmpty();
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        assertThat(firmwareService.isResumeFirmwareUploadEnabled(deviceType)).isFalse();
    }

    @Test
    @Transactional
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
    @Transactional
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

    @Test
    @Transactional
    public void testSupportedFirmwareTypes() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds();
        assertThat(firmwareService.getSupportedFirmwareTypes(deviceType)).containsOnly(FirmwareType.METER);

        DeviceProtocol protocol = deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol();

        when(protocol.supportsCommunicationFirmwareVersion()).thenReturn(true);
        assertThat(firmwareService.getSupportedFirmwareTypes(deviceType)).containsOnly(FirmwareType.METER, FirmwareType.COMMUNICATION);

        when(protocol.supportsCaConfigImageVersion()).thenReturn(true);
        assertThat(firmwareService.getSupportedFirmwareTypes(deviceType)).containsOnly(FirmwareType.METER, FirmwareType.COMMUNICATION, FirmwareType.CA_CONFIG_IMAGE);

        when(protocol.supportsCommunicationFirmwareVersion()).thenReturn(false);
        assertThat(firmwareService.getSupportedFirmwareTypes(deviceType)).containsOnly(FirmwareType.METER, FirmwareType.CA_CONFIG_IMAGE);
    }

    @Test
    @Transactional
    public void testGetDefaultChecks() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();

        List<FirmwareCheck> firmwareChecks = firmwareService.getFirmwareChecks().collect(Collectors.toList());
        assertThat(firmwareChecks).hasSize(5);
        assertThat(firmwareChecks.stream().map(FirmwareCheck::getName).collect(Collectors.toList())).containsExactly(Stream.of(
                FirmwareCheckTranslationKeys.MATCHING_TARGET_FIRMWARE_STATUS,
                FirmwareCheckTranslationKeys.NO_GHOST_FIRMWARE,
                FirmwareCheckTranslationKeys.MINIMUM_LEVEL_FIRMWARE,
                FirmwareCheckTranslationKeys.NO_DOWNGRADE,
                FirmwareCheckTranslationKeys.MASTER_HAS_LATEST_FIRMWARE
        ).map(FirmwareCheckTranslationKeys::getDefaultFormat).toArray(String[]::new));
    }

    @Test
    @Transactional
    public void testAddGetChecks() {
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();

        int defaultChecksNumber = (int) firmwareService.getFirmwareChecks().count();

        FirmwareCheck customCheck = mock(FirmwareCheck.class);
        firmwareService.addFirmwareCheck(customCheck);

        assertThat(firmwareService.getFirmwareChecks().collect(Collectors.toList()))
                .hasSize(defaultChecksNumber + 1)
                .contains(customCheck);

        firmwareService.removeFirmwareCheck(customCheck);
        assertThat(firmwareService.getFirmwareChecks().collect(Collectors.toList()))
                .hasSize(defaultChecksNumber)
                .doesNotContain(customCheck);
    }

    @Test
    @Transactional
    public void testIsInUse() {
        DeviceType deviceType = inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class)
                .newDeviceType("MyDeviceType", mockProtocolPluggableClass());
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        deviceConfiguration.activate();
        Device device = inMemoryPersistence.getInjector().getInstance(DeviceService.class)
                .newDevice(deviceConfiguration, "MyDevice", "mridOfMyDevice", Instant.now());

        FirmwareService firmwareService = inMemoryPersistence.getFirmwareService();
        FirmwareVersion firmwareVersion = firmwareService.newFirmwareVersion(deviceType, "firmwareVersion1", FirmwareStatus.FINAL, FirmwareType.METER, "firmwareVersion1")
                .initFirmwareFile(new byte[]{11}).create();

        assertThat(firmwareService.isFirmwareVersionInUse(firmwareVersion.getId())).isFalse();

        LocalDateTime now = LocalDateTime.now();
        Instant oneWeekAgo = Instant.ofEpochSecond(now.minus(1, ChronoUnit.WEEKS).toEpochSecond(ZoneOffset.UTC));

        ActivatedFirmwareVersion activatedFirmwareVersion = firmwareService.newActivatedFirmwareVersionFrom(device, firmwareVersion, Interval.of(Range.atLeast(oneWeekAgo)));
        activatedFirmwareVersion.save();

        assertThat(firmwareService.isFirmwareVersionInUse(firmwareVersion.getId())).isTrue();
    }

    private DeviceType getMockedDeviceTypeWithMessageIds(DeviceMessageId... deviceMessageIds) {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mockProtocolPluggableClass(deviceMessageIds);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        return deviceType;
    }

    private DeviceProtocolPluggableClass mockProtocolPluggableClass(DeviceMessageId... deviceMessageIds) {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        List<DeviceMessageSpec> deviceMessageSpecs = mockMessages(deviceMessageIds);
        when(deviceProtocol.getSupportedMessages()).thenReturn(deviceMessageSpecs);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        return deviceProtocolPluggableClass;
    }

    private List<DeviceMessageSpec> mockMessages(DeviceMessageId... deviceMessageIds) {
        List<com.energyict.mdc.upl.messages.DeviceMessageSpec> result = new ArrayList<>();
        for (DeviceMessageId deviceMessageId : deviceMessageIds) {
            com.energyict.mdc.upl.messages.DeviceMessageSpec spec = mock(com.energyict.mdc.upl.messages.DeviceMessageSpec.class);
            when(spec.getId()).thenReturn(deviceMessageId.dbValue());
            DeviceMessageCategory category = mock(DeviceMessageCategory.class);
            when(spec.getCategory()).thenReturn(category);
            when(category.getId()).thenReturn(getCategoryId(deviceMessageId));
            result.add(spec);
        }
        return result;
    }

    private int getCategoryId(DeviceMessageId deviceMessageId) {
        switch ((int) deviceMessageId.dbValue() / 1000) {
            case 5:
                return FirmwareServiceImpl.FIRMWARE_DEVICE_MESSAGE_CATEGORY_ID;
            default:
                return 1;
        }
    }
}
