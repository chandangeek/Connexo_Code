/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
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
        ServiceCategory serviceCategory = meteringService.getServiceCategory(serviceKind).get();
        com.elster.jupiter.metering.UsagePointBuilder usagePointBuilder = serviceCategory.newUsagePoint(getName(), installationTime)
                .withIsVirtual(false)
                .withIsSdp(true)
                .withLocation(location)
                .withGeoCoordinates(geoCoordinates);
        serviceCategory.getCustomPropertySets().stream()
                .filter(rcps -> rcps.getCustomPropertySet().isRequired())
                .forEach(rcps -> setRequiredProperties(usagePointBuilder, rcps));
        UsagePoint usagePoint = usagePointBuilder.create();
        switch (serviceCategory.getKind()) {
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

    private void setRequiredProperties(com.elster.jupiter.metering.UsagePointBuilder builder, RegisteredCustomPropertySet registeredCustomPropertySet) {
        CustomPropertySetValues values;
        CustomPropertySet<?, ?> customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        if (customPropertySet.isVersioned()) {
            values = CustomPropertySetValues.emptyFrom(installationTime);
        } else {
            values = CustomPropertySetValues.empty();
        }
        customPropertySet.getPropertySpecs().stream()
                .filter(PropertySpec::isRequired)
                .forEach(propertySpec -> {
                    if (propertySpec.getPossibleValues() != null && !propertySpec.getPossibleValues().getAllValues().isEmpty()) {
                        values.setProperty(propertySpec.getName(), propertySpec.getPossibleValues().getAllValues().get(0));
                    } else {
                        values.setProperty(propertySpec.getName(), propertySpec.getValueFactory().fromStringValue(null));//works ok for 'prepay'
                    }
                });
        builder.addCustomPropertySetValues(registeredCustomPropertySet, values);
    }
}
