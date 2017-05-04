/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.aggregation.CalculatedReadingRecord;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.aggregation.ReadingQuality;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.metering.aggregation.console", service = DataAggregationCommands.class, property = {
        "osgi.command.scope=dag",
        "osgi.command.function=aggregate",
        "osgi.command.function=activateMetrologyConfig",
        "osgi.command.function=linkMetrologyConfig",
        "osgi.command.function=setMultiplierValue",
        "osgi.command.function=matchingChannels",
        "osgi.command.function=showData",
        "osgi.command.function=introspect",
        "osgi.command.function=updateReading",
        "osgi.command.function=confirmReading",
        "osgi.command.function=estimateReading",
        "osgi.command.function=removeReading"
}, immediate = true)
@SuppressWarnings("unused")
public class DataAggregationCommands {

    private volatile DataAggregationService dataAggregationService;
    private volatile MeteringService meteringService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile Clock clock;

    @Reference
    public void setDataAggregationService(DataAggregationService dataAggregationService) {
        this.dataAggregationService = dataAggregationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void aggregate() {
        System.out.println("Usage: aggregate <usage point name> <contract purpose> <deliverable name> <start date> [<user name>]");
    }

    public void aggregate(String usagePointName, String contractPurpose, String deliverableName, String startDate) {
        this.aggregate(usagePointName, contractPurpose, deliverableName, startDate, "root");
    }

    public void aggregate(String usagePointName, String contractPurpose, String deliverableName, String startDate, String userName) {
        User user = this.userService.findUser(userName).orElseThrow(() -> new IllegalArgumentException("User with name " + userName + " does not exist"));
        this.aggregate(usagePointName, contractPurpose, deliverableName, startDate, user);
    }

    private void aggregate(String usagePointName, String contractPurpose, String deliverableName, String startDate, User user) {
        this.threadPrincipalService.set(user);
        try (TransactionContext context = this.transactionService.getContext()) {
            UsagePoint usagePoint = this.meteringService.findUsagePointByName(usagePointName)
                    .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
            MetrologyConfiguration configuration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                    .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                    .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
            MetrologyContract contract = configuration.getContracts().stream()
                    .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No contract for purpose " + contractPurpose));
            ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                    .filter(d -> d.getName().equals(deliverableName))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Deliverable not found on contract"));

            Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
            CalculatedMetrologyContractData data = this.dataAggregationService.calculate(usagePoint, contract, Range.openClosed(start, Instant.now(this.clock)));

            List<CalculatedReadingRecord> dataForDeliverable = data.getCalculatedDataFor(deliverable);
            System.out.println("records found for deliverable:" + dataForDeliverable.size());
            context.commit();
        }
    }

    public void showData() {
        System.out.println("Usage: showData <usage point name> <contract purpose> <deliverable name> <start date> [<end date>] [<user name>]");
    }

    public void showData(String usagePointName, String contractPurpose, String deliverableName, String startDate) {
        Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        Range<Instant> period = Range.openClosed(start, Instant.now());
        this.showData(usagePointName, contractPurpose, deliverableName, period, "root");
    }

    public void showData(String usagePointName, String contractPurpose, String deliverableName, String startDate, String endDateOrUserName) {
        Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        try {
            Instant end = ZonedDateTime.ofInstant(Instant.parse(endDateOrUserName + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
            Range<Instant> period = Range.closedOpen(start, end);
            this.showData(usagePointName, contractPurpose, deliverableName, period, "root");
        } catch (DateTimeParseException e) {
            // Maybe 5th parameter was username instead of endDate
            Optional<User> user = this.userService.findUser(endDateOrUserName);
            if (!user.isPresent()) {
                // It was not a user so assuming date format is wrong
                throw e;
            } else {
                Range<Instant> period = Range.openClosed(start, Instant.now());
                this.showData(usagePointName, contractPurpose, deliverableName, period, user.get());
            }
        }
    }

    public void showData(String usagePointName, String contractPurpose, String deliverableName, String startDate, String endDate, String userName) {
        Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        Instant end = ZonedDateTime.ofInstant(Instant.parse(endDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        Range<Instant> period = Range.closedOpen(start, end);
        this.showData(usagePointName, contractPurpose, deliverableName, period, userName);
    }

    private void showData(String usagePointName, String contractPurpose, String deliverableName, Range<Instant> period, String userName) {
        User user = this.userService.findUser(userName).orElseThrow(() -> new IllegalArgumentException("User with name " + userName + " does not exist"));
        this.showData(usagePointName, contractPurpose, deliverableName, period, user);
    }

    private void showData(String usagePointName, String contractPurpose, String deliverableName, Range<Instant> period, User user) {
        threadPrincipalService.set(user);
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePointByName(usagePointName)
                    .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
            UsagePointMetrologyConfiguration configuration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                    .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                    .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
            MetrologyContract contract = configuration.getContracts().stream()
                    .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                    .findFirst()
                    .orElseThrow(() -> noContractForPurpose(contractPurpose, configuration));
            ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                    .filter(d -> d.getName().equals(deliverableName))
                    .findFirst()
                    .orElseThrow(() -> deliverableNotAvailableInContract(deliverableName, contract));

            CalculatedMetrologyContractData data = dataAggregationService.calculate(usagePoint, contract, period);

            List<CalculatedReadingRecord> dataForDeliverable = data.getCalculatedDataFor(deliverable);
            dataForDeliverable.forEach(this::showReading);
            System.out.println("records found for deliverable:" + dataForDeliverable.size());
            context.commit();
        }
    }

    public void introspect() {
        System.out.println("Usage: introspect <usage point name> <contract purpose> <deliverable name> <start date> [<end date>]");
    }

    public void introspect(String usagePointName, String contractPurpose, String deliverableName, String startDate) {
        Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        Range<Instant> period = Range.openClosed(start, Instant.now());
        this.introspect(usagePointName, contractPurpose, deliverableName, period);
    }

    public void introspect(String usagePointName, String contractPurpose, String deliverableName, String startDate, String endDate) {
        Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        Instant end = ZonedDateTime.ofInstant(Instant.parse(endDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        Range<Instant> period = Range.closedOpen(start, end);
        this.introspect(usagePointName, contractPurpose, deliverableName, period);
    }

    private void introspect(String usagePointName, String contractPurpose, String deliverableName, Range<Instant> period) {
        UsagePoint usagePoint = meteringService.findUsagePointByName(usagePointName)
                .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
        UsagePointMetrologyConfiguration configuration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
        MetrologyContract contract = configuration.getContracts().stream()
                .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                .findFirst()
                .orElseThrow(() -> noContractForPurpose(contractPurpose, configuration));
        ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                .filter(d -> d.getName().equals(deliverableName))
                .findFirst()
                .orElseThrow(() -> deliverableNotAvailableInContract(deliverableName, contract));

        MetrologyContractCalculationIntrospector introspector = dataAggregationService.introspect(usagePoint, contract, period);
        System.out.println("Channels usages for deliverable " + deliverableName);
        introspector.getChannelUsagesFor(deliverable).stream()
                .map(chan -> String.valueOf(chan.getChannel().getId()) + ": " + chan.getChannel().getMainReadingType().getFullAliasName())
                .forEach(System.out::println);
    }

    public void removeReading() {
        System.out.println("Usage: removeReading <usage point name> <contract purpose> <deliverable name> <interval date> (YYY-MM-DD) <interval timestamp> (HH:MM:DD)");
    }

    public void removeReading(String usagePointName, String contractPurpose, String deliverableName, String intervalDate, String intervalTimestamp) {
        Instant timestamp = ZonedDateTime.ofInstant(Instant.parse(intervalDate + "T" + intervalTimestamp + "Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        this.removeReading(usagePointName, contractPurpose, deliverableName, timestamp);
    }

    private void removeReading(String usagePointName, String contractPurpose, String deliverableName, Instant intervalTimestamp) {
        UsagePoint usagePoint = meteringService.findUsagePointByName(usagePointName)
                .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
        UsagePointMetrologyConfiguration configuration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
        MetrologyContract contract = configuration.getContracts().stream()
                .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                .findFirst()
                .orElseThrow(() -> noContractForPurpose(contractPurpose, configuration));
        ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                .filter(d -> d.getName().equals(deliverableName))
                .findFirst()
                .orElseThrow(() -> deliverableNotAvailableInContract(deliverableName, contract));

        try (TransactionContext context = transactionService.getContext()) {
            dataAggregationService
                    .edit(usagePoint, contract, deliverable, QualityCodeSystem.MDM)
                    .remove(intervalTimestamp)
                    .save();
            context.commit();
        }
    }

    public void updateReading() {
        System.out.println("Usage: updateReading <usage point name> <contract purpose> <deliverable name> <interval date> (YYY-MM-DD) <interval timestamp> (HH:MM:DD) <value>");
    }

    public void updateReading(String usagePointName, String contractPurpose, String deliverableName, String intervalDate, String intervalTimestamp, String value) {
        Instant timestamp = ZonedDateTime.ofInstant(Instant.parse(intervalDate + "T" + intervalTimestamp + "Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        this.updateReading(usagePointName, contractPurpose, deliverableName, timestamp, new BigDecimal(value));
    }

    private void updateReading(String usagePointName, String contractPurpose, String deliverableName, Instant intervalTimestamp, BigDecimal value) {
        UsagePoint usagePoint = meteringService.findUsagePointByName(usagePointName)
                .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
        UsagePointMetrologyConfiguration configuration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
        MetrologyContract contract = configuration.getContracts().stream()
                .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                .findFirst()
                .orElseThrow(() -> noContractForPurpose(contractPurpose, configuration));
        ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                .filter(d -> d.getName().equals(deliverableName))
                .findFirst()
                .orElseThrow(() -> deliverableNotAvailableInContract(deliverableName, contract));

        try (TransactionContext context = transactionService.getContext()) {
            dataAggregationService
                    .edit(usagePoint, contract, deliverable, QualityCodeSystem.MDM)
                    .update(IntervalReadingImpl.of(intervalTimestamp, value))
                    .save();
            context.commit();
        }
    }

    public void estimateReading() {
        System.out.println("Usage: estimateReading <usage point name> <contract purpose> <deliverable name> <interval date> (YYY-MM-DD) <interval timestamp> (HH:MM:DD) <value>");
    }

    public void estimateReading(String usagePointName, String contractPurpose, String deliverableName, String intervalDate, String intervalTimestamp, String value) {
        Instant timestamp = ZonedDateTime.ofInstant(Instant.parse(intervalDate + "T" + intervalTimestamp + "Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        this.estimateReading(usagePointName, contractPurpose, deliverableName, timestamp, new BigDecimal(value));
    }

    private void estimateReading(String usagePointName, String contractPurpose, String deliverableName, Instant intervalTimestamp, BigDecimal value) {
        UsagePoint usagePoint = meteringService.findUsagePointByName(usagePointName)
                .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
        UsagePointMetrologyConfiguration configuration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
        MetrologyContract contract = configuration.getContracts().stream()
                .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                .findFirst()
                .orElseThrow(() -> noContractForPurpose(contractPurpose, configuration));
        ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                .filter(d -> d.getName().equals(deliverableName))
                .findFirst()
                .orElseThrow(() -> deliverableNotAvailableInContract(deliverableName, contract));

        try (TransactionContext context = transactionService.getContext()) {
            dataAggregationService
                    .edit(usagePoint, contract, deliverable, QualityCodeSystem.MDM)
                    .estimate(IntervalReadingImpl.of(intervalTimestamp, value))
                    .save();
            context.commit();
        }
    }

    public void confirmReading() {
        System.out.println("Usage: confirmReading <usage point name> <contract purpose> <deliverable name> <interval date> (YYY-MM-DD) <interval timestamp> (HH:MM:DD)");
    }

    public void confirmReading(String usagePointName, String contractPurpose, String deliverableName, String intervalDate, String intervalTimestamp) {
        Instant timestamp = ZonedDateTime.ofInstant(Instant.parse(intervalDate + "T" + intervalTimestamp + "Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        this.confirmReading(usagePointName, contractPurpose, deliverableName, timestamp);
    }

    private void confirmReading(String usagePointName, String contractPurpose, String deliverableName, Instant intervalTimestamp) {
        UsagePoint usagePoint = meteringService.findUsagePointByName(usagePointName)
                .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
        UsagePointMetrologyConfiguration configuration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration)
                .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
        MetrologyContract contract = configuration.getContracts().stream()
                .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                .findFirst()
                .orElseThrow(() -> noContractForPurpose(contractPurpose, configuration));
        ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                .filter(d -> d.getName().equals(deliverableName))
                .findFirst()
                .orElseThrow(() -> deliverableNotAvailableInContract(deliverableName, contract));

        try (TransactionContext context = transactionService.getContext()) {
            dataAggregationService
                    .edit(usagePoint, contract, deliverable, QualityCodeSystem.MDM)
                    .confirm(IntervalReadingImpl.of(intervalTimestamp, null))
                    .save();
            context.commit();
        }
    }

    private NoSuchElementException noContractForPurpose(String purpose, UsagePointMetrologyConfiguration configuration) {
        String availableContracts = configuration
                .getContracts()
                .stream()
                .map(MetrologyContract::getMetrologyPurpose)
                .map(MetrologyPurpose::getName)
                .collect(Collectors.joining(", "));
        System.out.println("available contracts: " + availableContracts);
        return new NoSuchElementException("No contract for purpose " + purpose);
    }

    private NoSuchElementException deliverableNotAvailableInContract(String deliverableName, MetrologyContract contract) {
        String availableDeliverables = contract
                .getDeliverables()
                .stream()
                .map(ReadingTypeDeliverable::getName)
                .collect(Collectors.joining(", "));
        System.out.println("available deliverables: " + availableDeliverables);
        return new NoSuchElementException("Deliverable not found on contract");
    }

    private void showReading(CalculatedReadingRecord readingRecord) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.UK)
                        .withZone(ZoneId.systemDefault());

        if (readingRecord.isPartOfTimeOfUseGap()) {
            System.out.println(
                    formatter.format(readingRecord.getTimeStamp()) + " time of use gap (event code= " + readingRecord.getTimeOfUseEvent().get().getCode() + ")");
        } else {
            List<? extends ReadingQualityRecord> qualities = readingRecord.getReadingQualities();
            Range<Instant> range = readingRecord.getTimePeriod().get();
            if (qualities.isEmpty()) {
                System.out.println(
                        formatter.format(readingRecord.getTimeStamp()) + " in " + range + " : " + readingRecord.getValue());
            } else {
                System.out.println(
                        formatter.format(readingRecord.getTimeStamp()) + " in " + range + " : " + readingRecord.getValue()
                                + " , " + ReadingQuality.getReadingQuality(qualities.get(0).getType().getCode()).toString());
            }
        }
    }

    private String getValue(BaseReadingRecord reading) {
        Quantity quantity = reading.getQuantity(reading.getReadingType());
        if (quantity != null) {
            return quantity.getValue().toString();
        } else {
            return "";
        }
    }

    public void activateMetrologyConfig(long metrologyConfigId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new NoSuchElementException("No such metrology configuration"))
                    .activate();

            context.commit();
        }
    }

    public void linkMetrologyConfig(String usagePointName, long metrologyConfigId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePointByName(usagePointName)
                    .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
            UsagePointMetrologyConfiguration configuration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .filter(mc -> mc instanceof UsagePointMetrologyConfiguration)
                    .map(UsagePointMetrologyConfiguration.class::cast)
                    .orElseThrow(() -> new NoSuchElementException("No such metrology configuration"));
            usagePoint.apply(configuration);
            context.commit();
        }
    }

    public void setMultiplierValue(String meterName, String standardMultiplierType, long value) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MeterActivation meterActivation = meteringService.findMeterByName(meterName)
                    .orElseThrow(() -> new NoSuchElementException("No such meter"))
                    .getCurrentMeterActivation()
                    .orElseThrow(() -> new NoSuchElementException("No current meter activation"));
            MultiplierType multiplierType = meteringService.getMultiplierType(MultiplierType.StandardType.valueOf(standardMultiplierType));
            meterActivation.setMultiplier(multiplierType, BigDecimal.valueOf(value));
            context.commit();
        }
    }

    public void matchingChannels(String meterName, long metrologyConfigId, String requirementName) {
        metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                .orElseThrow(() -> new NoSuchElementException("No such metrology configuration"))
                .getRequirements().stream()
                .filter(rq -> rq.getName().equals(requirementName))
                .findFirst().orElseThrow(() -> new NoSuchElementException("No such requirement"))
                .getMatchingChannelsFor(meteringService.findMeterByName(meterName)
                        .orElseThrow(() -> new NoSuchElementException("No such meter"))
                        .getCurrentMeterActivation()
                        .map(MeterActivation::getChannelsContainer)
                        .orElseThrow(() -> new NoSuchElementException("No current meter activation")))
                .stream()
                .map(ch -> ch.getMainReadingType().getMRID() + " " + ch.getMainReadingType().getFullAliasName())
                .forEach(System.out::println);
    }

}
