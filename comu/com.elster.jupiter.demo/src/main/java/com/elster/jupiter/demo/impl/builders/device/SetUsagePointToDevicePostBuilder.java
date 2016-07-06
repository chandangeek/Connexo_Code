package com.elster.jupiter.demo.impl.builders.device;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.builders.UsagePointBuilder;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Postbuilder setting a {@link UsagePoint} for the {@link Device}
 * Copyrights EnergyICT
 * Date: 29/09/2015
 * Time: 14:10
 */
public class SetUsagePointToDevicePostBuilder implements Consumer<Device> {

    private static int newUsagePointId = 0;
    private MeteringService meteringService;
    private Clock clock;
    private UsagePointBuilder usagePointBuilder;

    @Inject
    SetUsagePointToDevicePostBuilder(MeteringService meteringService, Clock clock) {
        this.meteringService = meteringService;
        this.clock = clock;
        this.usagePointBuilder = new UsagePointBuilder(meteringService);
    }

    @Override
    public void accept(Device device) {
        Log.write(this.usagePointBuilder.withMRID(newMRID()).withName(device.getName())
                .withInstallationTime(clock.instant())
                .withLocation(device.getLocation().orElse(null))
                .withGeoCoordinates(device.getSpatialCoordinates().orElse(null)));
        this.usagePointBuilder.create();
        setUsagePoint(device, this.usagePointBuilder.get());
    }

    private String newMRID() {
        return String.format("UP_%04d", ++newUsagePointId);
    }

    private void setUsagePoint(Device device, UsagePoint usagePoint) {
        Optional<Meter> meter = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())
                .flatMap(amrSystem -> amrSystem.findMeter("" + device.getId()));
        meter.ifPresent(mtr -> {
            System.out.println("==> activating usage point for meter " + mtr.getMRID());
            usagePoint.activate(mtr, clock.instant());
        });
    }

}
