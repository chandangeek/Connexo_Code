/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FirmwareManagementOptionsImplIT extends PersistenceTest {
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    private DeviceType deviceType;
    private FirmwareService firmwareService;

    @Before
    @Transactional
    public void setup() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        deviceType = inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class).newDeviceType("MyDeviceType", deviceProtocolPluggableClass);
        firmwareService = inMemoryPersistence.getFirmwareService();
    }

    @Test
    @Transactional
    public void testSaveGetDeleteOptions() {
        assertThat(firmwareService.findFirmwareManagementOptions(deviceType)).isEmpty();
        assertThat(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).isEmpty();

        FirmwareManagementOptions options = createOptions();
        assertThat(options.getOptions()).containsOnly(ProtocolSupportedFirmwareOptions.values());
        assertThat(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).containsOnly(ProtocolSupportedFirmwareOptions.values());

        options.setOptions(EnumSet.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE));
        options.save();
        assertThat(rereadOptions().getOptions()).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        assertThat(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).containsOnly(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);

        options.delete();
        assertThat(firmwareService.findFirmwareManagementOptions(deviceType)).isEmpty();
        assertThat(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).isEmpty();
    }

    @Test
    @Transactional
    public void testEmptyCheckOptions() {
        FirmwareManagementOptions options = createOptions();
        assertAllChecksAreOff(options);

        options.activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK, Collections.emptySet());
        options.activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK, Collections.emptySet());
        options.save();
        assertAllChecksAreOff(rereadOptions());

        Arrays.stream(FirmwareCheckManagementOption.values()).forEach(options::deactivate);
        options.save();
        assertAllChecksAreOff(rereadOptions());
    }

    @Test
    @Transactional
    public void testCheckOptions() {
        FirmwareManagementOptions options = createOptions();
        options.activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK, EnumSet.of(FirmwareStatus.FINAL));
        options.save();
        options = rereadOptions();
        assertThat(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).containsOnly(FirmwareStatus.FINAL);
        assertThat(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isFalse();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isNull();
        assertThat(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isFalse();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isEmpty();

        options.activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK, null);
        options.save();
        options = rereadOptions();
        assertThat(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).containsOnly(FirmwareStatus.FINAL);
        assertThat(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isNull();
        assertThat(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isFalse();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isEmpty();

        options.activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK, EnumSet.of(FirmwareStatus.TEST));
        options.save();
        options = rereadOptions();
        assertThat(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).containsOnly(FirmwareStatus.FINAL);
        assertThat(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isNull();
        assertThat(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).containsOnly(FirmwareStatus.TEST);

        options.activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK, EnumSet.of(FirmwareStatus.TEST, FirmwareStatus.FINAL));
        options.save();
        options = rereadOptions();
        assertThat(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).containsOnly(FirmwareStatus.FINAL);
        assertThat(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isNull();
        assertThat(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).containsOnly(FirmwareStatus.TEST, FirmwareStatus.FINAL);

        options.deactivate(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK);
        options.save();
        options = rereadOptions();
        assertThat(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isFalse();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isEmpty();
        assertThat(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isNull();
        assertThat(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).containsOnly(FirmwareStatus.TEST, FirmwareStatus.FINAL);

        options.deactivate(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK);
        options.save();
        options = rereadOptions();
        assertThat(options.isActivated(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isFalse();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.TARGET_FIRMWARE_STATUS_CHECK)).isEmpty();
        assertThat(options.isActivated(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isFalse();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK)).isNull();
        assertThat(options.isActivated(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).isTrue();
        assertThat(options.getStatuses(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK)).containsOnly(FirmwareStatus.TEST, FirmwareStatus.FINAL);

        options.deactivate(FirmwareCheckManagementOption.MASTER_FIRMWARE_CHECK);
        options.save();
        assertAllChecksAreOff(rereadOptions());
    }

    private FirmwareManagementOptions createOptions() {
        FirmwareManagementOptions options = firmwareService.newFirmwareManagementOptions(deviceType);
        options.setOptions(EnumSet.allOf(ProtocolSupportedFirmwareOptions.class));
        options.save();
        return rereadOptions();
    }

    private FirmwareManagementOptions rereadOptions() {
        Optional<FirmwareManagementOptions> foundOptions = firmwareService.findFirmwareManagementOptions(deviceType);
        assertThat(foundOptions).isPresent();
        return foundOptions.get();
    }

    private static void assertAllChecksAreOff(FirmwareManagementOptions options) {
        Arrays.stream(FirmwareCheckManagementOption.values()).forEach(checkOption -> {
            assertThat(options.isActivated(checkOption)).isFalse();
            if (checkOption == FirmwareCheckManagementOption.CURRENT_FIRMWARE_CHECK) {
                assertThat(options.getStatuses(checkOption)).isNull();
            } else {
                assertThat(options.getStatuses(checkOption)).isEmpty();
            }
        });
    }
}
