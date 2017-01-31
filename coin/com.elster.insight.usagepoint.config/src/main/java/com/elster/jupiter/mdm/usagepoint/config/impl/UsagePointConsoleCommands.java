/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.insight.usagepoint.config.console",
        service = UsagePointConsoleCommands.class,
        property = {"osgi.command.scope=usagepoint",
                "osgi.command.function=createMetrologyConfiguration",
                "osgi.command.function=renameMetrologyConfiguration",
                "osgi.command.function=deleteMetrologyConfiguration",
                "osgi.command.function=metrologyConfigurations",
                "osgi.command.function=linkUsagePointToMetrologyConfiguration",
                "osgi.command.function=createValidationRuleSet",
                "osgi.command.function=addValidationRuleSetToMetrologyContract",
                "osgi.command.function=createUsagePoint",
                "osgi.command.function=saveRegister",
                "osgi.command.function=saveLP",
                "osgi.command.function=getLpReadings"}, immediate = true)
public class UsagePointConsoleCommands {

    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile TransactionService transactionService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;
    private volatile Clock clock;

    public void createMetrologyConfiguration(String name, String serviceKindName) {
        try {
            Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(ServiceKind.valueOf(serviceKindName));
            if (serviceCategory.isPresent()) {
                transactionService.builder()
                        .principal(() -> "console")
                        .run(() -> metrologyConfigurationService.newMetrologyConfiguration(name, serviceCategory.get()));
            } else {
                System.out.println("No ServiceCategory for: " + serviceKindName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renameMetrologyConfiguration(long id, String name) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(id).get();
                        metrologyConfiguration.startUpdate().setName(name).complete();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMetrologyConfiguration(long id) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(id).get();
                        metrologyConfiguration.delete();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void metrologyConfigurations() {
        metrologyConfigurationService.findAllMetrologyConfigurations().forEach(System.out::println);
    }

    public void linkUsagePointToMetrologyConfiguration(String usagePointName, String metrologyConfigName) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        UsagePoint up = meteringService
                                .findUsagePointByName(usagePointName)
                                .orElseThrow(() -> new IllegalArgumentException("Usage Point " + usagePointName + " not found."));
                        UsagePointMetrologyConfiguration mc = metrologyConfigurationService
                                .findMetrologyConfiguration(metrologyConfigName)
                                .filter(config -> config instanceof UsagePointMetrologyConfiguration)
                                .map(UsagePointMetrologyConfiguration.class::cast)
                                .orElseThrow(() -> new IllegalArgumentException("Metrology configuration " + metrologyConfigName + " not found."));
                        up.apply(mc);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createValidationRuleSet(String name, String qualityCodeSystem) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        validationService.createValidationRuleSet(name, QualityCodeSystem.of(qualityCodeSystem));
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Add validation rule set to metrology contract")
    public void addValidationRuleSetToMetrologyContract(@Descriptor("Metrology contract id") long metrologyContractId, @Descriptor("Validation rule set id") long ruleSetId) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        MetrologyContract metrologyContract = metrologyConfigurationService
                                .findMetrologyContract(1L)
                                .orElseThrow(() -> new IllegalArgumentException("Metrology contract with id " + metrologyContractId + " not found."));
                        ValidationRuleSet validationRuleSet = validationService
                                .getValidationRuleSet(ruleSetId)
                                .orElseThrow(() -> new IllegalArgumentException("Rule set with id " + ruleSetId + " not found."));
                        usagePointConfigurationService.addValidationRuleSet(metrologyContract, validationRuleSet);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Create a usage point")
    public void createUsagePoint(@Descriptor("System id (usually 2)") long amrSystemId,
                                 @Descriptor("Meter ID") String amrId,
                                 @Descriptor("Name") String name) {
        transactionService.builder()
                .principal(() -> "console")
                .run(() -> {
                    AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId)
                            .orElseThrow(() -> new IllegalArgumentException("amr System not found"));
                    Meter meter = amrSystem.findMeter(amrId)
                            .orElseThrow(() -> new IllegalArgumentException("Usage Point not created : Meter not found " + amrId));
                    ServiceCategory category = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                            .orElseThrow(() -> new IllegalArgumentException("Could not get service"));
                    UsagePointBuilder builder = category.newUsagePoint(name, this.clock.instant());
                    UsagePoint up = builder.withIsSdp(true).withIsVirtual(false).create();
                    up.newElectricityDetailBuilder(Instant.now(clock)).withGrounded(YesNoAnswer.YES).withPhaseCode(PhaseCode.UNKNOWN).create();
                    up.linkMeters().activate(meter, metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT)).complete();
                    meter.update();
                    up.update();
                    System.out.println("Usage point " + up.getId() + " created with name: " + name);
                });
    }

    @Descriptor("Save a register reading")
    public void saveRegister(@Descriptor("System id (usually 2)") long amrSystemId,
                             @Descriptor("EA_MS Meter ID") String amrid,
                             @Descriptor("timestamp (2015-05-14T10:15:30Z)") String timestamp,
                             @Descriptor("CIM code") String cim,
                             @Descriptor("Value") double value) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        AmrSystem amrSystem = meteringService
                                .findAmrSystem(amrSystemId)
                                .orElseThrow(() -> new IllegalArgumentException("amr System not found"));
                        Meter meter = amrSystem
                                .findMeter(amrid)
                                .orElseThrow(() -> new IllegalArgumentException("Meter not found " + amrid));
                        meter.store(QualityCodeSystem.MDM, createReading(cim, BigDecimal.valueOf(value), Instant.parse(timestamp)));
                        System.out.println("Save register for ID: " + meter.getId());
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Save an LP readings")
    public void saveLP(@Descriptor("System id (usually 2)") long amrSystemId,
                       @Descriptor("EA_MS Meter ID") String amrid,
                       @Descriptor("timestamp (2015-05-14T10:15:30Z)") String timestamp,
                       @Descriptor("CIM code") String cim,
                       @Descriptor("Interval in minutes") int minutes,
                       @Descriptor("Comma separated Values") String values) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        AmrSystem amrSystem = meteringService
                                .findAmrSystem(amrSystemId)
                                .orElseThrow(() -> new IllegalArgumentException("amr System not found"));
                        Meter meter = amrSystem
                                .findMeter(amrid)
                                .orElseThrow(() -> new IllegalArgumentException("Meter not found " + amrid));
                        meter.store(QualityCodeSystem.MDM, createLPReading(cim, values, Instant.parse(timestamp), minutes));
                        System.out.println("Save LP for ID: " + meter.getId());
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MeterReading createReading(String cim, BigDecimal value, Instant timeStamp) {
        ReadingImpl reading = ReadingImpl.of(cim, value, timeStamp);
        return MeterReadingImpl.of(reading);
    }

    private MeterReading createLPReading(String cim, String values, Instant timeStamp, int minutes) {
        IntervalBlockImpl block = IntervalBlockImpl.of(cim);
        for (IntervalReading reading : getLpReadings(values, timeStamp, minutes)) {
            block.addIntervalReading(reading);
        }
        MeterReadingImpl results = MeterReadingImpl.newInstance();
        results.addIntervalBlock(block);
        return results;
    }

    private List<IntervalReading> getLpReadings(String values, Instant timeStamp, int minutes) {
        List<IntervalReading> results = new ArrayList<>();
        for (String value : values.split(",")) {
            try {
                results.add(IntervalReadingImpl.of(timeStamp, new BigDecimal(value)));
            } catch (Exception e) {
                System.out.println("Illegal value : " + value);
                throw e;
            }
            timeStamp = timeStamp.plus(minutes, ChronoUnit.MINUTES);
        }
        return results;
    }

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

}
