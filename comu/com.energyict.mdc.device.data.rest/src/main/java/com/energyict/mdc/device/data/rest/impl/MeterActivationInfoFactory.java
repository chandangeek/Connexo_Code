package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public class MeterActivationInfoFactory {

    public MeterActivationInfo asInfo(MeterActivation meterActivation, Device device) {
        MeterActivationInfo info = new MeterActivationInfo();
        info.id = meterActivation.getId();
        info.version = meterActivation.getVersion();
        info.active = meterActivation.isCurrent();
        info.start = meterActivation.getStart();
        info.end = meterActivation.getEnd();
        info.usagePoint = meterActivation.getUsagePoint().map(up -> new IdWithNameInfo(up.getId(), up.getName())).orElse(null);
        info.multiplier = device.getMultiplierAt(meterActivation.getStart()).orElse(null);
        info.deviceConfiguration = getEffectiveDeviceConfigurationInfo(meterActivation, device);
        return info;
    }

    private IdWithNameInfo getEffectiveDeviceConfigurationInfo(MeterActivation meterActivation, Device device) {
        if (meterActivation.getEnd() == null) {
            return new IdWithNameInfo(device.getDeviceConfiguration());
        }
        Instant middle = Instant.ofEpochMilli((meterActivation.getCreateDate().toEpochMilli() + meterActivation.getModificationDate().toEpochMilli()) / 2);
        DeviceConfiguration deviceConfiguration = device.getHistory(middle).map(Device::getDeviceConfiguration).orElseGet(device::getDeviceConfiguration);
        return new IdWithNameInfo(deviceConfiguration);
    }
}
