package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Additional behaviour related to 'Kore' objects when a new Device is saved
 */
public class SynchNewDeviceWithKore extends AbstractSyncDeviceWithKoreMeter {

    private DeviceImpl device;

    public SynchNewDeviceWithKore(DeviceImpl device, Optional<Instant> startDate, MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(meteringService, readingTypeUtilService, eventService, device.getCreateTime() == null ? clock.instant() : startDate.isPresent() ? startDate.get() : device.getCreateTime());
        this.device = device;
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        this.setDevice(device);

        createKoreMeterConfiguration(false);
        // create a new MeterActivation
        MeterActivation activation = this.activateMeter(getStart());
        // add Kore Channels for all MDC Channels and registers
        addKoreChannelsIfNecessary(activation);

        device.getKoreHelper().setCurrentMeterActivation(Optional.ofNullable(activation));
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false; //No current meter activation a new meter activation needs to be created
    }

    protected MeterActivation doActivateMeter(Instant generalizedStartDate) {
        if (getDevice().getUsagePoint().isPresent()) {
            return getDevice().getMeter().get().activate(device.getUsagePoint().get(), generalizedStartDate);
        }
        return getDevice().getMeter().get().activate(generalizedStartDate);
    }
}
