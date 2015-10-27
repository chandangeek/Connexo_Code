package com.elster.insight.usagepoint.config.impl;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.apache.felix.service.command.Descriptor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

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
                "osgi.command.function=getLpReadings"}, immediate = true)
public class ConsoleCommands {

    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;

    public void createMetrologyConfiguration(String name) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                usagePointConfigurationService.newMetrologyConfiguration(name);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void renameMetrologyConfiguration(long id, String name) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).get();
                metrologyConfiguration.setName(name);
                metrologyConfiguration.update();
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }
    
    public void deleteMetrologyConfiguration(long id) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                MetrologyConfiguration metrologyConfiguration = usagePointConfigurationService.findMetrologyConfiguration(id).get();
                metrologyConfiguration.delete();
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void metrologyConfigurations() {
       usagePointConfigurationService.findAllMetrologyConfigurations().stream().forEach(System.out::println);
    }
    
    public void linkUsagePointToMetrologyConfiguration(String usagePointMRID, String metrologyConfigName) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                UsagePoint up = meteringService.findUsagePoint(usagePointMRID).orElseThrow(() -> new IllegalArgumentException("Usage Point " + usagePointMRID + " not found."));
                MetrologyConfiguration mc = usagePointConfigurationService.findMetrologyConfiguration(metrologyConfigName).orElseThrow(() -> new IllegalArgumentException("Metrology configuration " + metrologyConfigName + " not found."));
                usagePointConfigurationService.link(up, mc);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }

    }
    
    public void createValidationRuleSet(String name) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                validationService.createValidationRuleSet(name);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }
    
    public void assignValRuleSetToMetrologyConfig(String metrologyConfigName, String ruleSetName) {
        threadPrincipalService.set(() -> "console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                
                MetrologyConfiguration mc = usagePointConfigurationService.findMetrologyConfiguration(metrologyConfigName).orElseThrow(() -> new IllegalArgumentException("Metrology configuration " + metrologyConfigName + " not found."));
                ValidationRuleSet vrs = validationService.getValidationRuleSet(ruleSetName).orElseThrow(() -> new IllegalArgumentException("Rule set " + ruleSetName + " not found."));
                mc.addValidationRuleSet(vrs);
                
            }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }
    
    @Descriptor("Create a meter")
    public void createMeter(@Descriptor("System id (usually 2)") long amrSystemId, @Descriptor("EA_MS Meter ID") String amrid, @Descriptor("MRID") String mrId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId).orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            Meter meter = amrSystem.newMeter(amrid).setName(amrid).setMRID(mrId).create();
            meter.update();
            context.commit();
            System.out.println("Meter " + amrid + " created with ID: " + meter.getId());
        } finally {
            threadPrincipalService.clear();
        }
    }

    @Descriptor("Create a usage point")
    public void createUsagePoint(@Descriptor("System id (usually 2)") long amrSystemId, @Descriptor("Meter ID") String mrId, @Descriptor("UsagePoint ID") String upId, @Descriptor("Name") String name) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId).orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            Meter meter = amrSystem.findMeter(mrId).orElseThrow(() -> new IllegalArgumentException("Usage Point not created : Meter not found " + mrId));
            ServiceCategory category = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).orElseThrow(() -> new IllegalArgumentException("Could not get service"));
            UsagePointBuilder builder = category.newUsagePoint(upId);
            UsagePoint up = builder.withName(name).withIsSdp(true).withIsVirtual(false).create();
            up.newElectricityDetailBuilder(Instant.now()).
                    withAmiBillingReady(AmiBillingReadyKind.BILLINGAPPROVED).
                    withConnectionState(UsagePointConnectedKind.CONNECTED).
                    withGrounded(true).
                    withPhaseCode(PhaseCode.UNKNOWN).build();
            meter.activate(up, Instant.parse("2014-01-01T08:00:00Z"));
            meter.update();
            up.update();
            context.commit();
            System.out.println("Usage point " + up.getId() + " created with name: " + name);
        } finally {
            threadPrincipalService.clear();
        }
    }

    @Descriptor("Save a register reading")
    public void saveRegister(@Descriptor("System id (usually 2)") long amrSystemId,
            @Descriptor("EA_MS Meter ID") String amrid,
            @Descriptor("timestamp (2015-05-14T10:15:30Z)") String timestamp,
            @Descriptor("CIM code") String cim,
            @Descriptor("Value") double value) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId).orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            Meter meter = amrSystem.findMeter(amrid).orElseThrow(() -> new IllegalArgumentException("Meter not found " + amrid));
            meter.store(createReading(cim, BigDecimal.valueOf(value), Instant.parse(timestamp)));
            context.commit();
            System.out.println("Save register for ID: " + meter.getId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    @Descriptor("Save an LP readings")
    public void saveLP(@Descriptor("System id (usually 2)") long amrSystemId,
            @Descriptor("EA_MS Meter ID") String amrid,
            @Descriptor("timestamp (2015-05-14T10:15:30Z)") String timestamp,
            @Descriptor("CIM code") String cim,
            @Descriptor("Interval in minutes") int minutes,
            @Descriptor("Comma separated Values") String values) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId).orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            Meter meter = amrSystem.findMeter(amrid).orElseThrow(() -> new IllegalArgumentException("Meter not found " + amrid));
            meter.store(createLPReading(cim, values, Instant.parse(timestamp), minutes));
            context.commit();
            System.out.println("Save LP for ID: " + meter.getId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
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

    private ArrayList<IntervalReading> getLpReadings(String values, Instant timeStamp, int minutes) {
        ArrayList<IntervalReading> results = new ArrayList<>();
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
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setMetringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }
}