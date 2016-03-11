package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.config.ExpressionNode;
import com.elster.jupiter.metering.impl.config.ExpressionNodeParser;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.metering.console", service = ConsoleCommands.class, property = {
        "osgi.command.scope=metering",
        "osgi.command.function=printDdl",
        "osgi.command.function=meters",
        "osgi.command.function=usagePoints",
        "osgi.command.function=readingTypes",
        "osgi.command.function=createMeter",
        "osgi.command.function=createUsagePoint",
        "osgi.command.function=channelConfig",
        "osgi.command.function=meterActivations",
        "osgi.command.function=renameMeter",
        "osgi.command.function=activateMeter",
        "osgi.command.function=addUsagePointToCurrentMeterActivation",
        "osgi.command.function=endCurrentMeterActivation",
        "osgi.command.function=advanceStartDate",
        "osgi.command.function=explain",
        "osgi.command.function=addEvents",
        "osgi.command.function=addFormula",
        "osgi.command.function=formulas"
}, immediate = true)
public class ConsoleCommands {

    private volatile ServerMeteringService meteringService;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;

    public void printDdl() {
        try {
            for (Table<?> table : dataModel.getTables()) {
                for (Object s : table.getDdl()) {
                    System.out.println(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void meters() {
        meteringService.getMeterQuery().select(Condition.TRUE).stream()
                .map(meter -> meter.getId() + " " + meter.getMRID())
                .forEach(System.out::println);
    }

    public void usagePoints() {
        meteringService.getUsagePointQuery().select(Condition.TRUE).stream()
                .map(usagePoint -> usagePoint.getId() + " " + usagePoint.getMRID())
                .forEach(System.out::println);
    }

    public void meterActivations(long meterId) {
        Meter meter = meteringService.findMeter(meterId).orElseThrow(() -> new IllegalArgumentException("Meter not found."));
        System.out.println(meter.getMeterActivations().stream()
                .map(this::toString)
                .collect(java.util.stream.Collectors.joining("\n")));
    }

    public void explain(String readingType) {
        System.out.println(explained(meteringService.getReadingType(readingType).orElseThrow(() -> new IllegalArgumentException("ReadingType does not exist."))));
    }

    private String toString(MeterActivation meterActivation) {
        String channels = meterActivation.getChannels().stream()
                .map(channel -> channel.getId() + " " + channel.getMainReadingType().getMRID())
                .collect(java.util.stream.Collectors.joining("\n\t"));
        return meterActivation.getRange().toString() + "\n\t" + channels;
    }

    public void createMeter(long amrSystemId, String amrid, String mrId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId).orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            amrSystem.newMeter(amrid)
                    .setMRID(mrId)
                    .create();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void renameMeter(String mrId, String newName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            meter.setName(newName);
            meter.update();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void activateMeter(String mrId, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            Instant activationDate = Instant.ofEpochMilli(epochMilli);
            meter.activate(activationDate);
            meter.update();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }
    public void addUsagePointToCurrentMeterActivation() {
        System.out.println("usage:");
        System.out.println("       addUsagePointToCurrentMeterActivation <mrid> <usagepoint mrid>");
    }

    public void addUsagePointToCurrentMeterActivation(String mrId, String usagePointmrId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            UsagePoint usagePoint = meteringService.findUsagePoint(usagePointmrId).get();
            MeterActivation meterActivation = meter.getCurrentMeterActivation().get();
            ((MeterActivationImpl) meterActivation).setUsagePoint(usagePoint);
            ((MeterActivationImpl) meterActivation).save();
            ((UsagePointImpl) usagePoint).adopt((MeterActivationImpl) meterActivation);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void endCurrentMeterActivation(String mrId, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            Instant endDate = Instant.ofEpochMilli(epochMilli);
            meter.getCurrentMeterActivation().get().endAt(endDate);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void advanceStartDate(String mrId, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            Instant newStartDate = Instant.ofEpochMilli(epochMilli);
            meter.getCurrentMeterActivation().get().advanceStartDate(newStartDate);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void createUsagePoint() {
        System.out.println("Usage: createUsagePoint <mRID>");
    }

    public void createUsagePoint(String mrId, String timestamp) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            context.commit();
            meteringService.getServiceCategory(ServiceKind.WATER).get().newUsagePoint(mrId, Instant.parse(timestamp)).create();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void readingTypes() {
        meteringService.getAvailableReadingTypes().stream()
                .map(IdentifiedObject::getMRID)
                .forEach(System.out::println);
    }

    public void addEvents(String mrId, String dataFile) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            File eventData = new File(dataFile);
            try (Scanner scanner = new Scanner(eventData)) {
                List<String> lines = new ArrayList<>();
                while (scanner.hasNextLine()){
                    lines.add(scanner.nextLine());
                }

                List<EndDeviceEventImpl> deviceEvents = lines.stream()
                        .map(line -> line.split(";"))
                        .map(line -> EndDeviceEventImpl.of(line[1], ZonedDateTime.parse(line[0], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")).toInstant()))
                        .collect(Collectors.toList());

                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                meterReading.addAllEndDeviceEvents(deviceEvents);
                meteringService.findMeter(mrId).get().store(meterReading);

                context.commit();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void addFormula(String formulaString) {
        try (TransactionContext context = transactionService.getContext()) {
            ExpressionNode node = new ExpressionNodeParser(meteringService.getThesaurus()).parse(formulaString);
            Formula formula = metrologyConfigurationService.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();
            context.commit();
        }
    }

    public void formulas() {
        metrologyConfigurationService.findFormulas().stream()
                .map(Formula::toString)
                .forEach(System.out::println);
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
        this.dataModel = meteringService.getDataModel();
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    private String explained(ReadingType readingType) {
        StringBuilder builder = new StringBuilder();
        builder.append("Macro period             : ").append(readingType.getMacroPeriod().getDescription()).append('\n');
        builder.append("Aggregate                : ").append(readingType.getAggregate().getDescription()).append('\n');
        builder.append("Measuring period         : ").append(readingType.getMeasuringPeriod().getDescription()).append('\n');
        builder.append("Accumulation             : ").append(readingType.getAccumulation().getDescription()).append('\n');
        builder.append("Flow direction           : ").append(readingType.getFlowDirection().getDescription()).append('\n');
        builder.append("Commodity                : ").append(readingType.getCommodity().getDescription()).append('\n');
        builder.append("Macro period             : ").append(readingType.getMacroPeriod().getDescription()).append('\n');
        builder.append("Measurement kind         : ").append(readingType.getMeasurementKind().getDescription()).append('\n');
        builder.append("IH numerator             : ").append(readingType.getInterharmonic().getNumerator()).append('\n');
        builder.append("IH denominator           : ").append(readingType.getInterharmonic().getDenominator()).append('\n');
        builder.append("Argument numerator       : ").append(readingType.getArgument().getNumerator()).append('\n');
        builder.append("Argument denominator     : ").append(readingType.getArgument().getDenominator()).append('\n');
        builder.append("Time of use              : ").append(readingType.getTou()).append('\n');
        builder.append("CPP                      : ").append(readingType.getCpp()).append('\n');
        builder.append("Consumption tier         : ").append(readingType.getConsumptionTier()).append('\n');
        builder.append("Phases                   : ").append(readingType.getPhases().getDescription()).append('\n');
        builder.append("Multiplier               : ").append(readingType.getMultiplier()).append('\n');
        builder.append("Unit                     : ").append(readingType.getUnit()).append('\n');
        builder.append("Currency                 : ").append(readingType.getCurrency().toString()).append('\n');
        return builder.toString();
    }


}
