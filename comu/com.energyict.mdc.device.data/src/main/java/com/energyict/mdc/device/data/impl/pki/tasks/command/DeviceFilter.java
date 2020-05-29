package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeterActivation;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import java.time.Instant;
import java.util.Optional;

public class DeviceFilter implements Command {

    private final Instant aTime;

    public DeviceFilter(Instant aTime) {
        this.aTime = aTime;
    }

    @Override
    public void run(SecurityAccessor securityAccessor) throws CommandAbortException {
        // following is according to old sql (join) and hopefully the same while code is complete s..t
        // unfortunately stage is not comparable (while one is marker interface and the other one has nothing to do with it) and therefore we need string comparisons
        Device device = securityAccessor.getDevice();
        if (device != null && EndDeviceStage.fromKey(device.getStage().getName()).equals(EndDeviceStage.OPERATIONAL)) {
            Optional<? extends MeterActivation> currentMeterActivation = device.getCurrentMeterActivation();
            if (currentMeterActivation.isPresent() && currentMeterActivation.get().isEffectiveAt(aTime)) {
                return;
            }
        }
        // breaking the commands unless above conditions are met: sec acc is linked to a device and activated
        throw new CommandAbortException("Could not find device or device not operational for expired security accessor:" + securityAccessor);
    }
}
