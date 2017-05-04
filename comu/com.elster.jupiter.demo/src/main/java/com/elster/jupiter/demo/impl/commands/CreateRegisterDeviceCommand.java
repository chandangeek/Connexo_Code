package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.DeviceConfigurationBuilder;
import com.elster.jupiter.demo.impl.builders.DeviceTypeBuilder;
import com.elster.jupiter.demo.impl.builders.MetrologyConfigurationBuilder;
import com.elster.jupiter.demo.impl.builders.ReadingTypeBuilder;
import com.elster.jupiter.demo.impl.builders.RegisterTypeBuilder;
import com.elster.jupiter.demo.impl.builders.UsagePointBuilder;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.RegisterType;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateRegisterDeviceCommand extends CommandWithTransaction {

    private final Clock clock;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Provider<ActivateDevicesCommand> activateDevicesCommandProvider;
    private final Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider;

    private String deviceName;

    @Inject
    public CreateRegisterDeviceCommand(Clock clock, MeteringService meteringService, MetrologyConfigurationService metrologyConfigurationService, Provider<ActivateDevicesCommand> activateDevicesCommandProvider, Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider) {
        this.clock = clock;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.activateDevicesCommandProvider = activateDevicesCommandProvider;
        this.addLocationInfoToDevicesCommandProvider = addLocationInfoToDevicesCommandProvider;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void run() {
        executeTransaction(() -> createRegisterDevice());
    }

    public void createRegisterDevice() {
        ImmutableMap<String, String> readingTypeMap = ImmutableMap.<String, String>builder()
                .put("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "Bulk A+")
                .put("0.0.0.12.0.1.158.0.0.0.0.0.0.0.0.0.29.0", "Active voltage")
                .put("8.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0", "Billing period A+")
                .put("8.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0", "Billing period A+")
                .put("0.8.0.0.1.1.12.0.0.0.0.0.0.0.0.0.38.0", "active energy max demand")
                .put("8.8.0.0.1.1.12.0.0.0.0.0.0.0.0.0.38.0", "Billing active energy max demand")
                .put("8.8.0.3.1.1.8.0.0.0.0.0.0.0.0.3.38.0", "Billing active energy cum. max demand")
                .build();


        //for non-existing register reading types - create reading type
        List<ReadingType> readingTypes = new ArrayList<>();
        int i = 0;
        for (String readingType : readingTypeMap.keySet()) {
            readingTypes.add(Builders.from(ReadingTypeBuilder.class).withMrid(readingType).withAlias(readingTypeMap.get(readingType)).get());
        }

        //create device type and add register types
        List<RegisterType> registerTypes = new ArrayList<>();
        i = 0;
        for (ReadingType readingType : readingTypes) {
            registerTypes.add(Builders.from(RegisterTypeBuilder.class).withObisCode("2.5.60.4.9.1" + ++i).withReadingType(readingType).get());
        }
        DeviceType deviceType = Builders.from(DeviceTypeBuilder.class)
                .withName(deviceName)
                .withProtocol("com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP")
                .withRegisterTypes(registerTypes)
                .get();

        //create device config and register configs
        DeviceConfiguration deviceConfiguration = Builders.from(DeviceConfigurationBuilder.class)
                .withName("Default")
                .withDeviceType(deviceType)
                .withRegisterTypes(deviceType.getRegisterTypes()).get();

        //activate config
        deviceConfiguration.activate();


        Instant activationDate = clock.instant().minus(30L, ChronoUnit.DAYS).plusSeconds(120);
        Device device = Builders.from(DeviceBuilder.class)
                .withName(deviceName)
                .withDeviceConfiguration(deviceConfiguration)
                .withSerialNumber(deviceName + "010001")
                .withShippingDate(activationDate)
                .withYearOfCertification(2014)
                .get();
        AddLocationInfoToDevicesCommand addLocationInfoToDevicesCommand = addLocationInfoToDevicesCommandProvider.get();
        addLocationInfoToDevicesCommand.setDevices(Collections.singletonList(device));
        addLocationInfoToDevicesCommand.run();

        ActivateDevicesCommand activateDevicesCommand = activateDevicesCommandProvider.get();
        activateDevicesCommand.setDevices(Collections.singletonList(device));
        activateDevicesCommand.setTransitionDate(activationDate);
        activateDevicesCommand.run();

        //create metrology config
        UsagePointMetrologyConfiguration metrologyConfiguration = Builders.from(MetrologyConfigurationBuilder.class)
                .withName(deviceName)
                .withServiceKind(ServiceKind.ELECTRICITY)
                .withReadingTypes(readingTypeMap.keySet().asList())
                .get();
        metrologyConfiguration.addMeterRole(metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT));

        //activate
        metrologyConfiguration.activate();

        //add usage point and link to meter/metrology config
        UsagePoint usagePoint = Builders.from(UsagePointBuilder.class)
                .withName(deviceName)
                .withServiceKind(ServiceKind.ELECTRICITY)
                .withInstallationTime(activationDate)
                .withLocation(device.getLocation().get())
                .get();
        usagePoint.apply(metrologyConfiguration, activationDate);

        this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())
                .flatMap(amrSystem -> amrSystem.findMeter(String.valueOf(device.getId())))
                .ifPresent(mtr -> usagePoint.linkMeters()
                        .activate(activationDate, mtr, this.metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                        .complete());

        //store readings for all registers
        device.getRegisters().stream().forEach(register -> addReadings(activationDate, register));
    }

    private void addReadings(Instant activationTime, Register register) {
        Instant readingTime = activationTime.plus(1L, ChronoUnit.DAYS);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        while (readingTime.isBefore(clock.instant())) {
            if (register.isBilling()) {
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(clock.instant(), ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0);
                meterReading.addReading(ReadingImpl.of(register.getReadingType().getMRID(), BigDecimal.valueOf(readingTime.getEpochSecond()), readingTime,
                        zonedDateTime.withDayOfMonth(1).toInstant(), zonedDateTime.plusMonths(1L).withDayOfMonth(1).toInstant()));
            } else {
                meterReading.addReading(ReadingImpl.of(register.getReadingType().getMRID(), BigDecimal.valueOf(readingTime.getEpochSecond()), readingTime));
            }
            readingTime = readingTime.plus(1L, ChronoUnit.DAYS);
        }
        register.getDevice().store(meterReading);
    }
}
