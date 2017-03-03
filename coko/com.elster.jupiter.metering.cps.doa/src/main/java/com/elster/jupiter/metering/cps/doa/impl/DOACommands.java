/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.doa.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-16 (12:47)
 */
@Component(name = "com.elster.jupiter.metering.aggregation.console", service = DOACommands.class, property = {
        "osgi.command.scope=doa",
        "osgi.command.function=linkCPS",
        "osgi.command.function=linkConsumptionAllocationCPS",
        "osgi.command.function=createSLP",
        "osgi.command.function=setSLPValues"
}, immediate = true)
@SuppressWarnings("unused")
public class DOACommands {

    private volatile Clock clock;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;
    private volatile SyntheticLoadProfileService slpService;

    @Reference
    @SuppressWarnings("unused")
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setSlpService(SyntheticLoadProfileService slpService) {
        this.slpService = slpService;
    }

    @SuppressWarnings("unused")
    public void linkCPS() {
        System.out.println("Usage: linkCPS <metrology configuration id> <custom property set id>");
    }

    @SuppressWarnings("unused")
    public void linkConsumptionAllocationCPS(long metrologyConfigurationId) {
        this.linkCPS(metrologyConfigurationId, ConsumptionAllocationCustomPropertySet.ID);
    }

    @SuppressWarnings("unused")
    public void linkCPS(long metrologyConfigurationId, String customPropertySetId) {
        this.threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = this.transactionService.getContext()) {
            MetrologyConfiguration configuration =
                    this.metrologyConfigurationService
                            .findMetrologyConfiguration(metrologyConfigurationId)
                            .orElseThrow(() -> new IllegalArgumentException("Metrology configuration with id " + metrologyConfigurationId + " does not exist"));
            configuration
                    .getCustomPropertySets()
                    .stream()
                    .filter(cps -> cps.getCustomPropertySetId().equals(customPropertySetId))
                    .findAny()
                    .orElseGet(() -> doLinkCPS(configuration, customPropertySetId));
            context.commit();
        }
    }

    private RegisteredCustomPropertySet doLinkCPS(MetrologyConfiguration configuration, String customPropertySetId) {
        this.threadPrincipalService.set(() -> "Console");
        RegisteredCustomPropertySet customPropertySet =
                this.customPropertySetService
                    .findActiveCustomPropertySet(customPropertySetId)
                    .orElseThrow(() -> new IllegalArgumentException("Custom property set with id '" + customPropertySetId + "' does not exist or is not active at this point in time (check your deployed bundles)."));
        configuration.addCustomPropertySet(customPropertySet);
        System.out.println("Custom property set is now linked to the specified metrology configuration");
        return customPropertySet;
    }

    @SuppressWarnings("unused")
    public void createSLP() {
        System.out.println("Usage: createSLP <name> [description] <reading type mRID> <start year>");
        System.out.println("       where <interval length> is one of: " + IntervalLength.names());
        System.out.println("       where <unit> is the name of one ReadingTypeUnit enum values");
    }

    @SuppressWarnings("unused")
    public void createSLP(String name, String readingTypeMRID, int startYear) {
        this.createSLP(name, name, readingTypeMRID, startYear);
    }

    @SuppressWarnings("unused")
    public void createSLP(String name, String description, String readingTypeMRID, int startYear) {
        this.threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = this.transactionService.getContext()) {
            ReadingType readingType = this.findReadingTypeOrThrowException(readingTypeMRID);
            SyntheticLoadProfile slp = this.slpService
                    .newSyntheticLoadProfile(
                            name,
                            Period.ofYears(1),
                            LocalDate.ofYearDay(startYear, 1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                            readingType)
                    .withDescription(description)
                    .build();
            System.out.println("Synthetic load profile created with id: " + slp.getId());
            context.commit();
        }
    }

    private ReadingType findReadingTypeOrThrowException(String readingTypeMRID) {
        List<ReadingType> readingTypes = this.meteringService.findReadingTypes(Collections.singletonList(readingTypeMRID));
        if (readingTypes.isEmpty()) {
            throw new IllegalArgumentException("Reading type does not exist: " + readingTypeMRID);
        } else {
            return readingTypes.get(0);
        }
    }

    @SuppressWarnings("unused")
    public void setSLPValues() {
        System.out.println("Usage: setSLPValues <id> <value> [<start year>]");
        System.out.println("       sets the specified <value> for each interval in the range");
        System.out.println("       (midnight of Jan 1st, <start year> (UTC)..midnight of Jan 1st, <next year from current clock> (UTC)");
        System.out.println("    or (midnight of Jan 1st, <start year> (UTC)..midnight of Jan 1st, <next year from start year> (UTC) if start year is in the future");
        System.out.println("       When start year is not specified it is taken from the start time of the SLP (assumed to be specified in UTC)");
    }

    @SuppressWarnings("unused")
    public void setSLPValues(long slpId, double value) {
        SyntheticLoadProfile slp = this.findSLPOrThrowException(slpId);
        this.setSLPValues(slp, value, LocalDateTime.ofInstant(slp.getStartTime(), ZoneOffset.UTC).getYear());
    }

    @SuppressWarnings("unused")
    public void setSLPValues(long slpId, double value, int startYear) {
        this.setSLPValues(this.findSLPOrThrowException(slpId), value, startYear);
    }

    private SyntheticLoadProfile findSLPOrThrowException(long id) {
        return this.slpService
                .findSyntheticLoadProfile(id)
                .orElseThrow(() -> new IllegalArgumentException("Synthetic load profile with id " + id + " does not exist"));
    }

    private void setSLPValues(SyntheticLoadProfile slp, double value, int startYear) {
        this.threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = this.transactionService.getContext()) {
            Map<Instant, BigDecimal> values = this.generateValues(slp, startYear, BigDecimal.valueOf(value));
            slp.addValues(values);
            System.out.println(values.keySet().size() + " values generated");
            context.commit();
        }
    }

    private Map<Instant, BigDecimal> generateValues(SyntheticLoadProfile slp, int startYear, BigDecimal value) {
        Year thisYear = Year.now(this.clock);
        LocalDate start = Year.of(startYear).atDay(1);
        LocalDate end;
        if (startYear > thisYear.getValue()) {
            // Start year is in the future, generate only that year
            end = start.plusYears(1);
        } else {
            end = thisYear.plusYears(1).atDay(1);
        }
        return IntervalLength
                .from(slp.getInterval())
                .toRange(start, end)
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        instant -> value));
    }

    private enum IntervalLength {
        MINUTE1(Duration.ofMinutes(1)),
        MINUTE2(Duration.ofMinutes(2)),
        MINUTE3(Duration.ofMinutes(3)),
        MINUTE4(Duration.ofMinutes(4)),
        MINUTE5(Duration.ofMinutes(5)),
        MINUTE6(Duration.ofMinutes(6)),
        MINUTE10(Duration.ofMinutes(10)),
        MINUTE12(Duration.ofMinutes(12)),
        MINUTE15(Duration.ofMinutes(15)),
        MINUTE20(Duration.ofMinutes(20)),
        MINUTE30(Duration.ofMinutes(30)),
        HOUR1(Duration.ofHours(1)),
        HOUR2(Duration.ofHours(2)),
        HOUR3(Duration.ofHours(3)),
        HOUR4(Duration.ofHours(4)),
        HOUR6(Duration.ofHours(6)),
        HOUR12(Duration.ofHours(12));

        private final Duration duration;

        IntervalLength(Duration duration) {
            this.duration = duration;
        }

        Duration toDuration() {
            return duration;
        }

        List<Instant> toRange(LocalDate startExclusive, LocalDate endInclusive) {
            ImmutableList.Builder<Instant> builder = ImmutableList.builder();
            LocalDateTime end = endInclusive.atStartOfDay();
            LocalDateTime current = startExclusive.atStartOfDay().plus(this.toDuration());
            while (!current.isAfter(end)) {
                builder.add(current.atZone(ZoneOffset.UTC).toInstant());
                current = current.plus(this.toDuration());
            }
            return builder.build();
        }

        static IntervalLength from(Duration duration) {
            return Stream
                    .of(values())
                    .filter(each -> each.toDuration().equals(duration))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Duration " + duration + " is not supported by the doa gogo command, likely because the SLP was not created with a doa command"));
        }

        static String names() {
            return Stream.of(values()).map(IntervalLength::name).collect(Collectors.joining(", "));
        }

    }

}