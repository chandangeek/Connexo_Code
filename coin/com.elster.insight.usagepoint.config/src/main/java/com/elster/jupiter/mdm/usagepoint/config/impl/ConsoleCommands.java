package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.transaction.TransactionService;
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

@Component(name = "com.elster.insight.usagepoint.config.console",
        service = ConsoleCommands.class,
        property = {"osgi.command.scope=usagepoint",
                "osgi.command.function=createMetrologyConfiguration",
                "osgi.command.function=renameMetrologyConfiguration",
                "osgi.command.function=deleteMetrologyConfiguration",
                "osgi.command.function=metrologyConfigurations",
                "osgi.command.function=linkUsagePointToMetrologyConfiguration",
                "osgi.command.function=createValidationRuleSet",
                "osgi.command.function=assignValRuleSetToMetrologyConfig",
                "osgi.command.function=createMeter",
                "osgi.command.function=createUsagePoint",
                "osgi.command.function=saveRegister",
                "osgi.command.function=saveLP",
                "osgi.command.function=getLpReadings",
                "osgi.command.function=activateValidation",
                "osgi.command.function=deactivateValidation"}, immediate = true)
public class ConsoleCommands {

    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile TransactionService transactionService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;
    private volatile Clock clock;

    public void createMetrologyConfiguration(String name) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> usagePointConfigurationService.newMetrologyConfiguration(name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renameMetrologyConfiguration(long id, String name) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).get();
                        metrologyConfiguration.updateName(name);
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
                        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).get();
                        metrologyConfiguration.delete();
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void metrologyConfigurations() {
        usagePointConfigurationService.findAllMetrologyConfigurations().stream().forEach(System.out::println);
    }

    public void linkUsagePointToMetrologyConfiguration(String usagePointMRID, String metrologyConfigName) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        UsagePoint up = meteringService
                                .findUsagePoint(usagePointMRID)
                                .orElseThrow(() -> new IllegalArgumentException("Usage Point " + usagePointMRID + " not found."));
                        MetrologyConfiguration mc = usagePointConfigurationService
                                .findMetrologyConfiguration(metrologyConfigName)
                                .orElseThrow(() -> new IllegalArgumentException("Metrology configuration " + metrologyConfigName + " not found."));
                        usagePointConfigurationService.link(up, mc);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createValidationRuleSet(String name) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        validationService.createValidationRuleSet(name);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assignValRuleSetToMetrologyConfig(String metrologyConfigName, String ruleSetName) {
        try {
            transactionService.builder()
                    .principal(() -> "console")
                    .run(() -> {
                        MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService
                                .findMetrologyConfiguration(metrologyConfigName)
                                .orElseThrow(() -> new IllegalArgumentException("Metrology configuration " + metrologyConfigName + " not found."));
                        ValidationRuleSet validationRuleSet = validationService
                                .getValidationRuleSet(ruleSetName)
                                .orElseThrow(() -> new IllegalArgumentException("Rule set " + ruleSetName + " not found."));
                        usagePointConfigurationService.addValidationRuleSet(metrologyConfiguration, validationRuleSet);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Create a meter")
    public void createMeter(@Descriptor("System id (usually 2)") long amrSystemId,
            @Descriptor("EA_MS Meter ID") String amrid,
            @Descriptor("MRID") String mrId) {
        transactionService.builder()
                .principal(() -> "console")
                .run(() -> {
                    AmrSystem amrSystem = meteringService
                            .findAmrSystem(amrSystemId)
                            .orElseThrow(() -> new IllegalArgumentException("amr System not found"));
                    Meter meter = amrSystem.newMeter(amrid).setName(amrid).setMRID(mrId).create();
                    meter.update();
                    System.out.println("Meter " + amrid + " created with ID: " + meter.getId());
                });
    }

    @Descriptor("Create a usage point")
    public void createUsagePoint(@Descriptor("System id (usually 2)") long amrSystemId,
            @Descriptor("Meter ID") String mrId,
            @Descriptor("UsagePoint ID") String upId,
            @Descriptor("Name") String name) {
        transactionService.builder()
        .principal(() -> "console")
        .run(() -> {
            AmrSystem amrSystem = meteringService
                    .findAmrSystem(amrSystemId)
                    .orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            Meter meter = amrSystem
                    .findMeter(mrId)
                    .orElseThrow(() -> new IllegalArgumentException("Usage Point not created : Meter not found " + mrId));
            ServiceCategory category = meteringService
                    .getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(() -> new IllegalArgumentException("Could not get service"));
            UsagePointBuilder builder = category.newUsagePoint(upId, this.clock.instant());
            UsagePoint up = builder.withName(name).withIsSdp(true).withIsVirtual(false).create();
            up.newElectricityDetailBuilder(Instant.now())
                    .withGrounded(true)
                    .withPhaseCode(PhaseCode.UNKNOWN).create();
            meter.activate(up, Instant.parse("2014-01-01T08:00:00Z"));
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
                meter.store(createReading(cim, BigDecimal.valueOf(value), Instant.parse(timestamp)));
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
                meter.store(createLPReading(cim, values, Instant.parse(timestamp), minutes));
                System.out.println("Save LP for ID: " + meter.getId());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Activate Validation on a Usage Point")
    public void activateValidation(@Descriptor("Usage Point MRID") String mrid,
            @Descriptor("lastChecked (2015-05-14T10:15:30Z)") String lastChecked) {
        try {
            transactionService.builder()
            .principal(() -> "console")
            .run(() -> {
                UsagePoint usagePoint = meteringService
                        .findUsagePoint(mrid)
                        .orElseThrow(() -> new IllegalArgumentException("Usage point not found with mrid " + mrid));
                Meter meter = usagePoint.getMeter(Instant.now())
                        .orElseThrow(() -> new IllegalArgumentException("Meter not found for usage point with mrid " + mrid));
                validationService.activateValidation(meter);
                validationService.updateLastChecked(usagePoint.getCurrentMeterActivation().get(), Instant.parse(lastChecked));
                System.out.println("Validation activated for meter: " + meter.getMRID() + " at usage point " + mrid);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Deactivate Validation on a Usage Point")
    public void deactivateValidation(@Descriptor("Usage Point MRID") String mrid) {
        try {
            transactionService.builder()
            .principal(() -> "console")
            .run(() -> {
                UsagePoint usagePoint = meteringService
                        .findUsagePoint(mrid)
                        .orElseThrow(() -> new IllegalArgumentException("Usage point not found with mrid " + mrid));
                Meter meter = usagePoint.getMeter(Instant.now())
                        .orElseThrow(() -> new IllegalArgumentException("Meter not found for usage point with mrid " + mrid));
                validationService.deactivateValidation(meter);
                System.out.println("Validation deactivated for meter: " + meter.getMRID() + " at usage point " + mrid);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MeterReading createReading(String cim, BigDecimal value, Instant timeStamp) {
        ReadingImpl reading = ReadingImpl.of(cim, value, timeStamp);
        MeterReading results = MeterReadingImpl.of(reading);
        return results;
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

}