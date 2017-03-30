/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.time.Clock;
import java.time.Instant;

/**
 * Additional behaviour related to 'Kore' objects when a Device multiplier is removed
 */
public class SyncDeviceWithKoreForRemoval extends AbstractSyncDeviceWithKoreMeter {

    public SyncDeviceWithKoreForRemoval(DeviceImpl device, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, clock.instant());
    }

    @Override
    protected MeterActivation doActivateMeter(Instant generalizedStartDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        setDevice(device);
        super.endCurrentMeterActivationIfPresent();
        getDevice().getMeter().get().makeObsolete();
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false;
    }
}
