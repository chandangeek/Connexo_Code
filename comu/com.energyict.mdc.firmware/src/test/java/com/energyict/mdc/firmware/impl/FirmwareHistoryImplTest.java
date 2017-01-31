/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceFirmwareHistory;
import com.energyict.mdc.firmware.DeviceFirmwareVersionHistoryRecord;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FirmwareHistoryImplTest extends PersistenceTest {
    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;
    private static final byte[] FIRMWARE_FILE = new byte[]{1,2,3,4,5,6,7,8};

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;

    private FirmwareService firmwareService;
    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;
    private Device device;

    private FirmwareVersion firmwareVersion1, firmwareVersion2, firmwareVersion3;
    private ActivatedFirmwareVersion activatedFirmwareVersion1, activatedFirmwareVersion2, activatedFirmwareVersion3;

    @Before
    @Transactional
    public void setup() {
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        deviceType = inMemoryPersistence.getInjector().getInstance(DeviceConfigurationService.class).newDeviceType("MyDeviceType", deviceProtocolPluggableClass);
        deviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        deviceConfiguration.activate();
        device = inMemoryPersistence.getInjector().getInstance(DeviceService.class).newDevice(deviceConfiguration ,"MyDevice", "mridOfMyDevice", Instant.now() );

        firmwareService = inMemoryPersistence.getFirmwareService();
        firmwareVersion1 = firmwareService.newFirmwareVersion(deviceType, "firmwareVersion1", FirmwareStatus.FINAL, FirmwareType.METER).initFirmwareFile(FIRMWARE_FILE).create();
        firmwareVersion2 = firmwareService.newFirmwareVersion(deviceType, "firmwareVersion2", FirmwareStatus.FINAL, FirmwareType.COMMUNICATION).initFirmwareFile(FIRMWARE_FILE).create();
        firmwareVersion3 = firmwareService.newFirmwareVersion(deviceType, "firmwareVersion3", FirmwareStatus.FINAL, FirmwareType.METER).initFirmwareFile(FIRMWARE_FILE).create();


        LocalDateTime now = LocalDateTime.now();
        Instant twoMonthsAgo = Instant.ofEpochSecond(now.minus(2, ChronoUnit.MONTHS).toEpochSecond(ZoneOffset.UTC));
        Instant oneMonthAgo = Instant.ofEpochSecond(now.minus(1, ChronoUnit.MONTHS).toEpochSecond(ZoneOffset.UTC));
        Instant oneWeekAgo = Instant.ofEpochSecond(now.minus(1, ChronoUnit.WEEKS).toEpochSecond(ZoneOffset.UTC));

        activatedFirmwareVersion1 = firmwareService.newActivatedFirmwareVersionFrom(device, firmwareVersion1, Interval.of(twoMonthsAgo, oneMonthAgo));
        activatedFirmwareVersion1.save();
        activatedFirmwareVersion2 = firmwareService.newActivatedFirmwareVersionFrom(device, firmwareVersion3, Interval.startAt(oneMonthAgo));
        activatedFirmwareVersion2.save();
        activatedFirmwareVersion3 = firmwareService.newActivatedFirmwareVersionFrom(device, firmwareVersion2, Interval.startAt(oneWeekAgo));
        activatedFirmwareVersion3.save();

    }

    @Test
    @Transactional
    public void firmwareVersionCountTest(){
        assertTrue(firmwareService.getActiveFirmwareVersion(device, FirmwareType.COMMUNICATION).isPresent());
        assertTrue(firmwareService.getActiveFirmwareVersion(device, FirmwareType.METER).isPresent());
    }
    @Test
    @Transactional
    public void historyTest(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceFirmwareHistory history = firmwareService.getFirmwareHistory(device);
        assertThat(history.history()).hasSize(3);
    }
    @Test
    @Transactional
    public void historySortTest(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceFirmwareHistory history = firmwareService.getFirmwareHistory(device);
        List<DeviceFirmwareVersionHistoryRecord> records = history.history();
        assertThat(records).hasSize(3);
        assertTrue(records.get(0).getInterval().startsAfter(records.get(1).getInterval().getStart()));
        assertTrue(records.get(1).getInterval().startsAfter(records.get(2).getInterval().getStart()));
    }

    @Test
    @Transactional
    public void historyTestForFirmwareType(){
        FirmwareServiceImpl firmwareService = inMemoryPersistence.getFirmwareService();
        DeviceFirmwareHistory history = firmwareService.getFirmwareHistory(device);
        assertThat(history.history(FirmwareType.METER)).hasSize(2);
        assertThat(history.history(FirmwareType.COMMUNICATION)).hasSize(1);
    }


}
