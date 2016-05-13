package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.*;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 03/06/15
 * Time: 11:17
 */
public class FirmwareVersionImplTest extends PersistenceTest{
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

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
    @ExpectedConstraintViolation(messageId = "{" + com.energyict.mdc.firmware.impl.MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
    public void uniqueVersionTest() {
        String version = "Version1";
        FirmwareVersion meterVersion1 = inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareVersion meterVersion2 = inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.METER).create();
    }


    @Test
    @Transactional
    public void uniqueVersionCheckButDifferentTypeTest() {
        String version = "Version1";
        FirmwareVersion meterVersion1 = inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.METER).create();
        FirmwareVersion meterVersion2 = inMemoryPersistence.getFirmwareService().newFirmwareVersion(deviceType, version, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION).create();

        FirmwareService firmwareService = inMemoryPersistence.getFirmwareService();
        List<FirmwareVersion> firmwareVersions = inMemoryPersistence.getFirmwareService().findAllFirmwareVersions(firmwareService.filterForFirmwareVersion(deviceType)).find();
        assertThat(firmwareVersions).hasSize(2);
        assertThat(firmwareVersions.get(0).getFirmwareVersion()).isEqualTo(version);
        assertThat(firmwareVersions.get(1).getFirmwareVersion()).isEqualTo(version);
    }


}