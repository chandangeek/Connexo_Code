/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDevice;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Additional behaviour related to 'Kore' objects when the Device's multiplier is changed
 */
public class SyncDeviceWithKoreForMultiplierChange extends AbstractSyncDeviceWithKoreMeter {

    private ServerDevice device;
    private BigDecimal multiplier;

    public SyncDeviceWithKoreForMultiplierChange(ServerDevice device, Instant start, BigDecimal multiplier, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, start);
        this.device = device;
        this.multiplier = multiplier;
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        this.setDevice(device);

        endCurrentMeterConfigurationIfPresent();
        createKoreMeterConfiguration();
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
        Optional<? extends MeterActivation> optionalMeterActivation = getDevice().getMeter().get().getMeterActivation(generalizedStartDate);
        if (optionalMeterActivation.isPresent()) {

            MeterActivation meterActivation;
            if (!optionalMeterActivation.get().getStart().equals(generalizedStartDate)) {
                meterActivation = endMeterActivationAndRestart(generalizedStartDate, optionalMeterActivation, Optional.empty());
            } else {
                meterActivation = optionalMeterActivation.get();
            }
            setMultiplier(meterActivation, multiplier);
            device.getKoreHelper().setCurrentMeterActivation(Optional.of(meterActivation));
            return meterActivation;
        }
        return device.getKoreHelper().getCurrentMeterActivation().get();
    }
}
