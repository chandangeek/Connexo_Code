package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDevice;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Additional behaviour related to 'Kore' objects when the Device's multiplier is changed
 */
public class SynchDeviceWithKoreForMultiplierChange extends AbstractSyncDeviceWithKoreMeter {

    private ServerDevice device;
    private BigDecimal multiplier;

    public SynchDeviceWithKoreForMultiplierChange(ServerDevice device, Instant start, BigDecimal multiplier, MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService) {
        super(meteringService, readingTypeUtilService, eventService, start);
        this.device = device;
        this.multiplier = multiplier;
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        this.setDevice(device);

        endCurrentMeterConfigurationIfPresent();
        createKoreMeterConfiguration(true);
        // create a new MeterActivation
        MeterActivation activation = activateMeter(getStart());
        // add Kore Channels for all MDC Channels and registers
        addKoreChannelsIfNecessary(activation);
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return true; // meter activation has a setMultiplier method;
    }

    protected MeterActivation doActivateMeter(Instant generalizedStartDate) {
        Optional<MeterActivation> meterActivation;
        // If the devices current meter activation starts at start, we just have to update this one!
        if (device.getKoreHelper().getCurrentMeterActivation().isPresent() && device.getKoreHelper()
                .getCurrentMeterActivation()
                .get()
                .getStart()
                .equals(generalizedStartDate)) {
            meterActivation = device.getKoreHelper().getCurrentMeterActivation();
        } else {
            meterActivation = Optional.of(getDevice().getMeter().get().getMeterActivation(generalizedStartDate).get());
        }
        meterActivation = Optional.of(endMeterActivationAndRestart(generalizedStartDate, meterActivation, Optional.empty()));
        setMultiplier(meterActivation.get(), multiplier);
        device.getKoreHelper().setCurrentMeterActivation(meterActivation);
        return meterActivation.get();
    }
}
