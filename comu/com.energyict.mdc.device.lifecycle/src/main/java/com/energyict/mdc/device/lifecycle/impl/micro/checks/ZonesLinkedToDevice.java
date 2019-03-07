package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.config.DefaultTransition;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class ZonesLinkedToDevice extends TranslatableServerMicroCheck {
    private final MeteringService meteringService;
    private final MeteringZoneService meteringZoneService;

    @Inject
    public ZonesLinkedToDevice(MeteringService meteringService, MeteringZoneService meteringZoneService) {
        this.meteringService = meteringService;
        this.meteringZoneService = meteringZoneService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.ZONES.name();
    }

    @Override
    public Set<DefaultTransition> getOptionalDefaultTransitions() {
        return EnumSet.of(DefaultTransition.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING,
                DefaultTransition.INSTALL_AND_ACTIVATE,
                DefaultTransition.ACTIVATE);
    }

    @Override
    public Optional<ExecutableMicroCheckViolation> evaluate(Device device, Instant effectiveTimestamp, State toState) {
        if (!anyLinkedZone(device).isPresent()) {
            return fail(MicroCheckTranslations.Message.AT_LEAST_ONE_ZONE_LINKED, device.getName());
        } else {
            return Optional.empty();
        }
    }

    private Optional<EndDeviceZone> anyLinkedZone(Device device) {
        return meteringZoneService.getByEndDevice(
                meteringService.findEndDeviceByMRID(device.getmRID()).get())
                .stream().findAny();
    }

}
