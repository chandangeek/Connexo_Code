package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

/**
 * {@link Builder} for creating {@link UsagePoint}
 *
*/
public class UsagePointBuilder extends NamedBuilder<UsagePoint, UsagePointBuilder>  {

    private MeteringService meteringService;
    private ServiceKind serviceKind = ServiceKind.ELECTRICITY;
    private Instant installationTime;
    private Location location;
    private SpatialCoordinates geoCoordinates;

    @Inject
    public UsagePointBuilder(MeteringService meteringService){
        super(UsagePointBuilder.class);
        this.meteringService = meteringService;
    }

    public UsagePointBuilder withInstallationTime(Instant installationTime){
        this.installationTime = installationTime;
        return this;
    }

    public UsagePointBuilder withServiceKind(ServiceKind serviceKind){
        this.serviceKind = serviceKind;
        return this;
    }

    public UsagePointBuilder withLocation(Location location){
        this.location = location;
        return this;
    }

    public UsagePointBuilder withGeoCoordinates(SpatialCoordinates geoCoordiantes) {
        this.geoCoordinates = geoCoordiantes;
        return this;
    }

    @Override
    public Optional<UsagePoint> find() {
        if (this.getName() == null) {
            throw new IllegalStateException("Name cannot be null");
        }
        return meteringService.findUsagePointByName(getName());
    }

    @Override
    public UsagePoint create() {
        UsagePoint usagePoint = meteringService.getServiceCategory(serviceKind).get().newUsagePoint(getName(), installationTime)
                .withIsVirtual(false)
                .withIsSdp(true)
                .withLocation(location)
                .withGeoCoordinates(geoCoordinates).create();
        switch (usagePoint.getServiceCategory().getKind()){
            case ELECTRICITY:
                usagePoint.newElectricityDetailBuilder(installationTime).withPhaseCode(PhaseCode.S1N).create();
                break;
            case GAS:
                usagePoint.newGasDetailBuilder(installationTime).create();
                break;
            case WATER:
                usagePoint.newWaterDetailBuilder(installationTime).create();
                break;
            case HEAT:
                usagePoint.newHeatDetailBuilder(installationTime).create();
                break;
            default:
                usagePoint.newDefaultDetailBuilder(installationTime).create();
        }
        return usagePoint;
    }
}
