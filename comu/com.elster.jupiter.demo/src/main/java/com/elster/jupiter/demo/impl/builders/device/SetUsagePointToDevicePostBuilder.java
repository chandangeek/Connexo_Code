package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.builders.UsagePointBuilder;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.util.units.Unit;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.math.BigDecimal;
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
    private MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    SetUsagePointToDevicePostBuilder(MeteringService meteringService, Clock clock, MetrologyConfigurationService metrologyConfigurationService) {
        this.meteringService = meteringService;
        this.clock = clock;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.usagePointBuilder = new UsagePointBuilder(meteringService);
    }

    @Override
    public void accept(Device device) {
        Log.write(this.usagePointBuilder.withMRID(newMRID()).withName(device.getName())
                .withInstallationTime(clock.instant())
                .withLocation(device.getLocation().orElse(null))
                .withGeoCoordinates(device.getSpatialCoordinates().orElse(null)));
        this.usagePointBuilder.create();
        this.usagePointBuilder.get().forCustomProperties().getPropertySetsOnServiceCategory().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointGeneralDomainExtensionValues(clock.instant())));
        this.usagePointBuilder.get().forCustomProperties().getAllPropertySets().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrDE".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointTechnicalInstallationDomainExtensionValues()));
        setUsagePoint(device, this.usagePointBuilder.get());
    }

    private CustomPropertySetValues getUsagePointGeneralDomainExtensionValues(Instant from) {
        CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(from);
        values.setProperty("prepay", false);
        values.setProperty("marketCodeSector", "Domestic");
        values.setProperty("meteringPointType", "E17 - Consumption");
        return values;
    }

    private CustomPropertySetValues getUsagePointTechnicalInstallationDomainExtensionValues() {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty("substationDistance", Unit.METER.amount(BigDecimal.ZERO));
        return values;
    }

    private String newMRID() {
        return String.format("UP_%04d", ++newUsagePointId);
    }

    private void setUsagePoint(Device device, UsagePoint usagePoint) {
        Optional<Meter> meter = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())
                .flatMap(amrSystem -> amrSystem.findMeter("" + device.getId()));
        meter.ifPresent(mtr -> {
            System.out.println("==> activating usage point for meter " + mtr.getMRID());
            usagePoint.linkMeters()
                    .activate(mtr, this.metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                    .complete();
        });
    }

}
