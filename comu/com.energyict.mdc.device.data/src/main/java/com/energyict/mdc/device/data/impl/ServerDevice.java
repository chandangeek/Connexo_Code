package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForInfo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 26.07.16
 * Time: 14:15
 */
public interface ServerDevice extends Device {
    Reference<Meter> getMeter();

    SyncDeviceWithKoreForInfo getKoreHelper();

    Map<MetrologyConfiguration, List<ReadingTypeRequirement>> getUnsatisfiedRequirements(UsagePoint usagePoint, Instant from, DeviceConfiguration deviceConfiguration);

    void touch();

    void activateEstimation();

    void deactivateEstimation();
}
