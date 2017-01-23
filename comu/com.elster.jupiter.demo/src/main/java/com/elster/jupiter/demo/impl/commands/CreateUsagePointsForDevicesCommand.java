package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.UsagePointBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.MetrologyConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.UserTpl;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.units.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateUsagePointsForDevicesCommand {
    private final ThreadPrincipalService threadPrincipalService;
    private final DeviceService deviceService;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Clock clock;

    private List<Device> devices;

    @Inject
    public CreateUsagePointsForDevicesCommand(ThreadPrincipalService threadPrincipalService,
                                              DeviceService deviceService,
                                              MeteringService meteringService,
                                              MetrologyConfigurationService metrologyConfigurationService,
                                              Clock clock) {
        this.threadPrincipalService = threadPrincipalService;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.clock = clock;
    }

    public CreateUsagePointsForDevicesCommand setDevices(List<Device> devices) {
        this.devices = Collections.unmodifiableList(devices);
        return this;
    }

    public void run() {
        getDeviceList().forEach(this::accept);
    }

    private List<Device> getDeviceList() {
        return this.devices != null ? this.devices : this.deviceService.deviceQuery().select(where("name").like(Constants.Device.STANDARD_PREFIX + "*"));
    }

    private void accept(Device device) {
        UsagePoint usagePoint = device.getUsagePoint()
                .orElseGet(newUsagePointSupplier(device));
        usagePoint.forCustomProperties().getPropertySetsOnServiceCategory().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointGeneralDomainExtensionValues(clock.instant())));
        usagePoint.forCustomProperties().getAllPropertySets().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrDE".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointTechnicalInstallationDomainExtensionValues()));
        if (device.getDeviceConfiguration().getName().equals(DeviceConfigurationTpl.CONSUMERS.getName())) {
            usagePoint.apply(Builders.from(MetrologyConfigurationTpl.CONSUMER).get());
        } else {
            usagePoint.apply(Builders.from(MetrologyConfigurationTpl.PROSUMER).get());
        }
        usagePoint.update();
        setUsagePoint(device, usagePoint);
    }

    private Supplier<UsagePoint> newUsagePointSupplier(Device device) {
        return () -> {
            Principal currentPrincipal = threadPrincipalService.getPrincipal();
            // need 'real' user to create usage point,
            // so that initial UsagePointState change request will be created with this user
            threadPrincipalService.set(Builders.from(UserTpl.MELISSA).get());
            UsagePoint usagePoint = Builders.from(UsagePointBuilder.class)
                    .withName(newName(device.getSerialNumber()))
                    .withInstallationTime(clock.instant())
                    .withLocation(device.getLocation().orElse(null))
                    .withGeoCoordinates(device.getSpatialCoordinates().orElse(null))
                    .get();
            threadPrincipalService.set(currentPrincipal);
            return usagePoint;
        };
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

    private String newName(String serialNumber) {
        return "UP_" + serialNumber;
    }

    private void setUsagePoint(Device device, UsagePoint usagePoint) {
        if (!device.getUsagePoint().isPresent()
                && !device.getState(this.clock.instant().plus(10, ChronoUnit.MINUTES)).map(State::isInitial).orElse(true)) {
            // +10m to be sure that we get the latest state and skip all devices with initial state
            this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())
                    .flatMap(amrSystem -> amrSystem.findMeter(String.valueOf(device.getId())))
                    .ifPresent(mtr -> usagePoint.linkMeters()
                            .activate(mtr, this.metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                            .complete());
        }
    }
}
