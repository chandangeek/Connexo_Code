package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.DeviceFirmwareVersionHistoryRecord;
import com.energyict.mdc.firmware.PassiveFirmwareVersion;

import java.time.Instant;

/**
 * Implementation for {@link DeviceFirmwareVersionHistoryRecord}
 * Copyrights EnergyICT
 * Date: 1/04/2016
 * Time: 14:17
 */
public class DeviceFirmwareVersionHistoryRecordImpl implements DeviceFirmwareVersionHistoryRecord {

    private Device device;
    private FirmwareVersion firmwareVersion;
    private Instant lastChecked;
    private Interval effectivityInterval;

    public DeviceFirmwareVersionHistoryRecordImpl(ActivatedFirmwareVersion activatedFirmwareVersion){
        this.device = activatedFirmwareVersion.getDevice();
        this.firmwareVersion = activatedFirmwareVersion.getFirmwareVersion();
        this.lastChecked = activatedFirmwareVersion.getLastChecked();
        this.effectivityInterval = activatedFirmwareVersion.getInterval();
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public FirmwareVersion getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public Instant getLastChecked() {
        return lastChecked;
    }

    @Override
    public Interval getInterval() {
        return effectivityInterval;
    }
}
