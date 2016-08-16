package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.time.Clock;
import java.time.Instant;

/**
 * Additional behaviour related to 'Kore' objects when a Device multiplier is removed
 */
public class SyncDeviceWithKoreForRemoval extends AbstractSyncDeviceWithKoreMeter {

    private DeviceImpl device;

    public SyncDeviceWithKoreForRemoval(DeviceImpl device, MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(meteringService, readingTypeUtilService, eventService, clock.instant());
        this.device = device;
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
