package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Additional behaviour related to 'Kore' objects when a new Device is saved
 */
public class SynchNewDeviceWithKore extends AbstractSyncDeviceWithKoreMeter {

    public SynchNewDeviceWithKore(DeviceImpl device, Optional<Instant> startDate, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, device.getCreateTime() == null ? clock.instant() : startDate
                .isPresent() ? startDate.get() : device.getCreateTime());
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
        Optional<? extends MeterActivation> meterActivation = getDevice().getCurrentMeterActivation();
        if (meterActivation.flatMap(MeterActivation::getUsagePoint).isPresent()) {
            return getDevice().getMeter()
                    .get()
                    .activate(meterActivation.flatMap(MeterActivation::getUsagePoint)
                            .get(), meterActivation.flatMap(MeterActivation::getMeterRole).get(), generalizedStartDate);
        }
        return getDevice().getMeter().get().activate(generalizedStartDate);
    }
}
