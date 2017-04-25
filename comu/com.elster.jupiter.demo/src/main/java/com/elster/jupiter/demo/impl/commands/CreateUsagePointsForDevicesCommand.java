/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.UsagePointBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.UserTpl;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.util.units.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateUsagePointsForDevicesCommand {
    private final ThreadPrincipalService threadPrincipalService;
    private final DeviceService deviceService;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointLifeCycleService usagePointLifeCycleService;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final Clock clock;

    private List<Device> devices;

    @Inject
    public CreateUsagePointsForDevicesCommand(ThreadPrincipalService threadPrincipalService,
                                              DeviceService deviceService,
                                              MeteringService meteringService,
                                              MetrologyConfigurationService metrologyConfigurationService,
                                              Clock clock,
                                              UsagePointLifeCycleService usagePointLifeCycleService,
                                              UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService
    ) {
        this.threadPrincipalService = threadPrincipalService;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.clock = clock;
        this.usagePointLifeCycleService = usagePointLifeCycleService;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    public CreateUsagePointsForDevicesCommand setDevices(List<Device> devices) {
        this.devices = Collections.unmodifiableList(devices);
        return this;
    }

    public void run() {
        getDeviceList(Constants.Device.STANDARD_PREFIX).forEach(this::acceptElectricityDevice);
        getDeviceList(Constants.Device.WATER_PREFIX).forEach(this::acceptWaterDevice);
        getDeviceList(Constants.Device.GAS_PREFIX).forEach(this::acceptGasDevice);
    }

    public void run(String prefix) {
        switch (prefix) {
            case Constants.Device.STANDARD_PREFIX:
                devices.stream().forEach(this::acceptElectricityDevice);
                break;
            case Constants.Device.WATER_PREFIX:
                devices.stream().forEach(this::acceptWaterDevice);
                break;
            case Constants.Device.GAS_PREFIX:
                devices.stream().forEach(this::acceptGasDevice);
                break;
        }
    }

    private List<Device> getDeviceList(String prefix) {
        //we need 80% of active devices
        List<Device> devices = this.deviceService.deviceQuery().select(where("name").like(prefix + "*")).stream().
                filter(device -> device.getState().getName().equals(DefaultState.ACTIVE.getKey()))
                .collect(Collectors.toList());

        double length = (double) devices.size();
        Collections.shuffle(devices);
        return devices.subList(0, (int) Math.floor(length / 10 * 8));
    }

    private void acceptElectricityDevice(Device device) {
        UsagePoint usagePoint = device.getUsagePoint()
                .orElseGet(newUsagePointSupplier(device, ServiceKind.ELECTRICITY, "SUPE"));
        usagePoint.forCustomProperties().getPropertySetsOnServiceCategory().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointGeneralDomainExtensionValues(clock.instant().plusSeconds(60))));
        usagePoint.forCustomProperties().getAllPropertySets().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrDE".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointTechnicalInstallationDomainExtensionValues()));
        UsagePointMetrologyConfiguration metrologyConfiguration;
        if (device.getDeviceConfiguration().getName().equals(DeviceConfigurationTpl.CONSUMERS.getName())) {
            metrologyConfiguration = (UsagePointMetrologyConfiguration) metrologyConfigurationService.findMetrologyConfiguration("Residential consumer with 1 meter").get();
        } else {
            metrologyConfiguration = (UsagePointMetrologyConfiguration) metrologyConfigurationService.findMetrologyConfiguration("Residential prosumer with 1 meter").get();
        }
        metrologyConfiguration.addMeterRole(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT));
        usagePoint.apply(metrologyConfiguration, clock.instant());
        usagePoint.update();
        setUsagePoint(device, usagePoint);
        activateUsagePoint(usagePoint);
    }

    private void acceptWaterDevice(Device device) {
        UsagePoint usagePoint = device.getUsagePoint()
                .orElseGet(newUsagePointSupplier(device, ServiceKind.WATER, "SUPW"));
        usagePoint.forCustomProperties().getPropertySetsOnServiceCategory().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointGeneralDomainExtensionValues(clock.instant().plusSeconds(60))));
        UsagePointMetrologyConfiguration metrologyConfiguration = (UsagePointMetrologyConfiguration) metrologyConfigurationService.findMetrologyConfiguration("Residential water").get();
        metrologyConfiguration.addMeterRole(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT));
        usagePoint.apply(metrologyConfiguration, clock.instant());
        usagePoint.update();
        setUsagePoint(device, usagePoint);
        activateUsagePoint(usagePoint);

    }

    private void acceptGasDevice(Device device) {
        UsagePoint usagePoint = device.getUsagePoint()
                .orElseGet(newUsagePointSupplier(device, ServiceKind.GAS, "SUPG"));
        usagePoint.forCustomProperties().getPropertySetsOnServiceCategory().stream()
                .filter(cps -> "com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension".equals(cps.getCustomPropertySet().getId()))
                .forEach(cps -> cps.setValues(getUsagePointGeneralDomainExtensionValues(clock.instant().plusSeconds(60))));
        UsagePointMetrologyConfiguration metrologyConfiguration = (UsagePointMetrologyConfiguration) metrologyConfigurationService.findMetrologyConfiguration("Residential gas").get();
        metrologyConfiguration.addMeterRole(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT));
        usagePoint.apply(metrologyConfiguration, clock.instant());
        usagePoint.update();
        setUsagePoint(device, usagePoint);
        activateUsagePoint(usagePoint);
    }

    private void activateUsagePoint(UsagePoint usagePoint){
        Principal currentPrincipal = threadPrincipalService.getPrincipal();
        threadPrincipalService.set(Builders.from(UserTpl.MELISSA).get());
        UsagePointTransition usagePointTransition = this.usagePointLifeCycleConfigurationService.findUsagePointTransition(1).get();
        Map<String, Object> propertiesMap = DecoratedStream.decorate(usagePointTransition.getActions().stream())
                .flatMap(microAction -> microAction.getPropertySpecs().stream())
                .distinct(PropertySpec::getName)
                .collect(Collectors.toMap(PropertySpec::getName, propertySpec -> propertySpec.getValueFactory().fromStringValue("CONNECTED")));
        usagePointLifeCycleService.performTransition(usagePoint, usagePointTransition,"INS",propertiesMap);
        threadPrincipalService.set(currentPrincipal);
    }

    private Supplier<UsagePoint> newUsagePointSupplier(Device device, ServiceKind serviceKind, String prefix) {
        return () -> {
            Principal currentPrincipal = threadPrincipalService.getPrincipal();
            // need 'real' user to create usage point,
            // so that initial State change request will be created with this user
            threadPrincipalService.set(Builders.from(UserTpl.MELISSA).get());
            UsagePoint usagePoint = Builders.from(UsagePointBuilder.class)
                    .withName(prefix + device.getSerialNumber())
                    .withInstallationTime(clock.instant())
                    .withLocation(device.getLocation().orElse(null))
                    .withGeoCoordinates(device.getSpatialCoordinates().orElse(null))
                    .withServiceKind(serviceKind)
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

    private void setUsagePoint(Device device, UsagePoint usagePoint) {
        if (!device.getUsagePoint().isPresent()
                && !device.getState(this.clock.instant().plus(10, ChronoUnit.MINUTES)).map(State::isInitial).orElse(true)) {
            // +10m to be sure that we get the latest state and skip all devices with initial state
            Instant now = clock.instant();
            this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())
                    .flatMap(amrSystem -> amrSystem.findMeter(String.valueOf(device.getId())))
                    .ifPresent(mtr -> usagePoint.linkMeters()
                            .activate(now, mtr, this.metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                            .complete());
            usagePoint.getEffectiveMetrologyConfiguration(now).ifPresent(effectiveMC -> effectiveMC.close(now));
            usagePoint.apply(metrologyConfiguration, now);
            usagePoint.update();
        }
    }
}
