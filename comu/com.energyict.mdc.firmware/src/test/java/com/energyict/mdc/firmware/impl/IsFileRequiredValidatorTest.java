/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 19/05/2016 - 14:55
 */
public class IsFileRequiredValidatorTest extends PersistenceTest {

    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final byte[] FIRMWARE_FILE = new byte[]{1, 2, 3, 4};
    private static final String VERSION = "Version";
    private static final String NEW_VERSION = "NewVersion";
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    private DeviceType deviceType;

    @Before
    @Transactional
    public void setup() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        deviceType = inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class).newDeviceType("MyDeviceType", deviceProtocolPluggableClass);
    }

    @Test
    @Transactional
    public void validateFirmwareVersionWithValidFirmwareFileTest() {
        inMemoryPersistence.getFirmwareService()
                .newFirmwareVersion(deviceType, VERSION, FirmwareStatus.FINAL, FirmwareType.METER)
                .initFirmwareFile(FIRMWARE_FILE)
                .create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void validateFirmwareVersionWithoutFirmwareFileTest() {
        inMemoryPersistence.getFirmwareService()
                .newFirmwareVersion(deviceType, VERSION, FirmwareStatus.FINAL, FirmwareType.METER)
                .create();  // Thus without the expected firmware size specified
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FILE_IS_EMPTY + "}")
    public void validateFirmwareVersionWithEmptyFirmwareSizeTest() {
        inMemoryPersistence.getFirmwareService()
                .newFirmwareVersion(deviceType, VERSION, FirmwareStatus.FINAL, FirmwareType.METER)
                .setExpectedFirmwareSize(0) // With invalid expected firmware size
                .create();
    }

    @Test
    @Transactional
    public void validateFirmwareVersionUpdateWithoutTouchingFirmwareFileTest() {
        FirmwareVersion firmwareVersion = inMemoryPersistence.getFirmwareService()
                .newFirmwareVersion(deviceType, VERSION, FirmwareStatus.FINAL, FirmwareType.METER)
                .initFirmwareFile(FIRMWARE_FILE)
                .create();

        FirmwareVersion reloadedFirmwareVersion = inMemoryPersistence.getFirmwareService().getFirmwareVersionById(firmwareVersion.getId()).get();
        reloadedFirmwareVersion.setFirmwareVersion(NEW_VERSION);
        reloadedFirmwareVersion.validate(); //This should succeed without any violations
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FILE_IS_EMPTY + "}")
    public void validateFirmwareVersionUpdatedWithEmptyFirmwareSizeTest() {
        FirmwareVersion firmwareVersion = inMemoryPersistence.getFirmwareService()
                .newFirmwareVersion(deviceType, VERSION, FirmwareStatus.FINAL, FirmwareType.METER)
                .setExpectedFirmwareSize(FIRMWARE_FILE.length)
                .create();  // This call should succeed without violations

        FirmwareVersion reloadedFirmwareVersion = inMemoryPersistence.getFirmwareService().getFirmwareVersionById(firmwareVersion.getId()).get();
        reloadedFirmwareVersion.setFirmwareVersion(NEW_VERSION);
        reloadedFirmwareVersion.setExpectedFirmwareSize(0);
        reloadedFirmwareVersion.validate();
    }
}