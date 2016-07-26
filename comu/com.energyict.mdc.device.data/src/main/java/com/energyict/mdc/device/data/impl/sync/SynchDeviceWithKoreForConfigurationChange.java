package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Additional behaviour related to 'Kore' objects when the Device's configuration is changed
 */
public class SynchDeviceWithKoreForConfigurationChange extends AbstractSyncDeviceWithKoreMeter {

    private DeviceImpl device;

    public SynchDeviceWithKoreForConfigurationChange(DeviceImpl device, MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, Clock clock) {
        super(meteringService, readingTypeUtilService, clock.instant());
        this.device = device;
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        this.setDevice(device);

        endCurrentMeterConfigurationIfPresent();
        createKoreMeterConfiguration(device.getKoreHelper().getMultiplier().isPresent());
        // create a new MeterActivation

        MeterActivation activation = activateMeter(getStart());
        // add Kore Channels for all MDC Channels and registers
        addKoreChannelsIfNecessary(activation);
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false; // meter activation has a setMultiplier method;
    }

    protected MeterActivation doActivateMeter(Instant generalizedStartDate) {
        Optional<MeterActivation> meterActivation;
        // If the devices current meter activation starts at start, we just have to update this one!
        if (device.getKoreHelper().getCurrentMeterActivation().isPresent() && device.getKoreHelper().getCurrentMeterActivation().get().getStart().equals(generalizedStartDate)) {
            meterActivation = device.getKoreHelper().getCurrentMeterActivation();
        } else {
            meterActivation = Optional.of(getDevice().getMeter().get().getMeterActivation(generalizedStartDate).get());
        }
        if (meterActivation.isPresent() && meterActivation.get().getStart().compareTo(generalizedStartDate) < 0) {
            meterActivation = Optional.of(endMeterActivationAndRestart(generalizedStartDate, meterActivation, Optional.empty()));
        }
        device.getKoreHelper().setCurrentMeterActivation(meterActivation);
        return meterActivation.get();
    }
}
