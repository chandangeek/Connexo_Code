package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;

import java.time.Instant;
import java.util.Optional;

public class ZonesLinkedToDevice extends TranslatableServerMicroCheck {
    // TODO: refactor

    MeteringService meteringService;
    MeteringZoneService meteringZoneService;

    public ZonesLinkedToDevice(Thesaurus thesaurus, MeteringService meteringService, MeteringZoneService meteringZoneService) {
        super(thesaurus);
        this.meteringService = meteringService;
        this.meteringZoneService = meteringZoneService;
    }

    @Override
    protected MicroCheck getMicroCheck() {
        return MicroCheck.AT_LEAST_ONE_ZONE_LINKED;
    }

    @Override
    public Optional<DeviceLifeCycleActionViolation> evaluate(Device device, Instant effectiveTimestamp) {
        if (!anyZonesLinked(device).isPresent()) {
            return Optional.of(
                    new DeviceLifeCycleActionViolationImpl(
                            this.thesaurus,
                            MessageSeeds.AT_LEAST_ONE_ZONE_LINKED,
                            MicroCheck.AT_LEAST_ONE_ZONE_LINKED));
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<EndDeviceZone> anyZonesLinked(Device device) {
        return meteringZoneService.getByEndDevice(
                meteringService.findEndDeviceByMRID(device.getmRID()).get())
                .stream().findAny();
    }

}
