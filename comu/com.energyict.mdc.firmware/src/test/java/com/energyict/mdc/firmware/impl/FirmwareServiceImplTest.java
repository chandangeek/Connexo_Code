package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        assertThat(options).isNotEmpty();
        assertThat(options).hasSize(1);
        assertThat(options.contains(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE)).isTrue();
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateLaterTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).isNotEmpty();
        assertThat(options).hasSize(1);
        assertThat(options.contains(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER)).isTrue();
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithOnlyActivateDateTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE);

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).isNotEmpty();
        assertThat(options).hasSize(1);
        assertThat(options.contains(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE)).isTrue();
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithActivateLaterAndActivateImmediateTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE
        );

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).isNotEmpty();
        assertThat(options).hasSize(2);
        assertThat(options.containsAll(Arrays.asList(
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE))).isTrue();
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithAllOptionsTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE
        );

        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        Set<ProtocolSupportedFirmwareOptions> options = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        assertThat(options).isNotEmpty();
        assertThat(options).hasSize(3);
        assertThat(options.containsAll(Arrays.asList(
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE))).isTrue();
    }

    @Test
    public void getProtocolSupportedFirmwareOptionsWithAllOptionsAndDuplicatesByTheProtocolTest() {
        DeviceType deviceType = getMockedDeviceTypeWithMessageIds(
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
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

        assertThat(options).isNotEmpty();
        assertThat(options).hasSize(3);
        assertThat(options.containsAll(Arrays.asList(
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE,
                ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE))).isTrue();
    }

    private DeviceType getMockedDeviceTypeWithMessageIds(DeviceMessageId... deviceMessageIds) {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.getSupportedMessages()).thenReturn(mockMessages(deviceMessageIds));
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