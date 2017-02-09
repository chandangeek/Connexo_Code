/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.IValidationRule;
import com.elster.jupiter.validation.impl.kpi.DeviceDataQualityKpiSqlBuilder.ResultSetColumn;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.elster.jupiter.util.streams.Predicates.not;

public class DeviceDataQualityKpiCalculator implements DataQualityKpiCalculator {

    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final TransactionService transactionService;
    private final Clock clock;
    private final DataModel dataModel;
    private final DataValidationKpiImpl dataValidationKpi;
    private final Logger logger;

    private Map<ReadingQualityType, ValidationRule> qualitiesToValidationRules;
    private Map<ReadingQualityType, EstimationRule> qualitiesToEstimationRules;
    private ZonedDateTime end;
    private ZonedDateTime start;
    private Map<Key, LongCounter> counterMap;
    private Map<Long, DataValidationKpiChild> dataValidationKpiChildMap;

    DeviceDataQualityKpiCalculator(ValidationService validationService, EstimationService estimationService,
                                   TransactionService transactionService, DataModel dataModel,
                                   DataValidationKpiImpl dataValidationKpi, Clock clock, Logger logger) {
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.transactionService = transactionService;
        this.dataModel = dataModel;
        this.dataValidationKpi = dataValidationKpi;
        this.clock = clock;
        this.logger = logger;
    }

    @Override
    public void calculateAndStore() {
        calculateInTransaction();
        this.dataValidationKpi.getDeviceGroup()
                .getMembers(this.clock.instant())
                .forEach(this::storeInTransaction);
    }

    private void calculateInTransaction() {
        try {
            transactionService.run(this::calculate);
        } catch (Exception ex) {
            transactionService.run(() -> logger.log(Level.WARNING, "Failed to calculate Data Validation KPI. Error: " + ex.getLocalizedMessage(), ex));
        }
    }

    private void storeInTransaction(EndDevice endDevice) {
        try {
            transactionService.run(() -> store(endDevice));
        } catch (Exception ex) {
            transactionService.run(() -> logger.log(Level.WARNING, "Failed to store Validation KPI data for device " + endDevice.getName()
                    + ". Error: " + ex.getLocalizedMessage(), ex));
        }
    }

    private void calculate() {
        dataValidationKpiChildMap = dataValidationKpi.updateMembers();
        if (dataValidationKpi.isCancelled()) {
            dataValidationKpi.dropDataValidationKpi();
            return;
        }
        qualitiesToValidationRules = mapQualitiesToValidationRules();
        qualitiesToEstimationRules = mapQualitiesToEstimationRules();
        end = now().with(LocalTime.MIDNIGHT).plusDays(1);
        start = end.minusMonths(1);
        counterMap = calculateFromQuery(start, end);
    }

    private ZonedDateTime now() {
        return this.clock.instant().atZone(this.clock.getZone());
    }

    private void store(EndDevice endDevice) {
        if (!(endDevice instanceof Meter)) {
            return;
        }
        Meter meter = (Meter) endDevice;
        Set<Channel> channels = meter.getMeterActivations(Range.openClosed(start.toInstant(), end.toInstant()))
                .stream()
                .map(MeterActivation::getChannelsContainer)
                .map(ChannelsContainer::getChannels)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Map<DataQualityKpiMemberTypes, Map<Instant, BigDecimal>> dataMap = new HashMap<>();
        ZonedDateTime calculateDate = start;
        while (!calculateDate.isAfter(end)) {

            Instant calculateInstant = calculateDate.toInstant();

            long registerSuspects = channels.stream()
                    .filter(not(Channel::isRegular))
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long channelSuspects = channels.stream()
                    .filter(Channel::isRegular)
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long totalSuspects = registerSuspects + channelSuspects;

            long missing = channels.stream()
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.MISSING))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long informative = channels.stream()
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.INFORMATIVE))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long added = channels.stream()
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.ADDED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long edited = channels.stream()
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.EDITED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long removed = channels.stream()
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.REMOVED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long estimated = channels.stream()
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.ESTIMATED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long confirmed = channels.stream()
                    .map(channelToKey(calculateDate, DefaultDataQualityMetric.CONFIRMED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            // TODO add validators
            // TODO add estimators

            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.CHANNEL, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(channelSuspects));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.REGISTER, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(registerSuspects));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.SUSPECT, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(totalSuspects));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.MISSINGVALUESVALIDATOR, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(missing));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.INFORMATIVE, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(informative));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.ADDED, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(added));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.EDITED, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(edited));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.REMOVED, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(removed));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.ESTIMATED, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(estimated));
            dataMap.computeIfAbsent(DataQualityKpiMemberTypes.CONFIRMED, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(confirmed));

            calculateDate = calculateDate.plusDays(1);
        }

        DataValidationKpiChild dataValidationKpiChild = dataValidationKpiChildMap.get(meter.getId());
        Map<KpiMember, Map<Instant, BigDecimal>> memberScores = new HashMap<>();
        dataMap.entrySet().forEach(entry -> {
            KpiMember entryMember = dataValidationKpiChild.getChildKpi()
                    .getMembers()
                    .stream()
                    .filter(kpiMember -> kpiMember.getName().toUpperCase().startsWith(entry.getKey().name()))
                    .findAny()
                    .get();
            memberScores.put(entryMember, entry.getValue());
        });

        dataValidationKpiChild.getChildKpi().store(memberScores);
    }

    private Function<Channel, Key> channelToKey(ZonedDateTime calculateDate, DataQualityMetric dataQualityMetric) {
        return channel -> new Key(channel.getId(), calculateDate.toLocalDate(), dataQualityMetric);
    }

    private final static class Key {

        private final long channelId;
        private final LocalDate localDate;
        private final DataQualityMetric dataQualityMetric;

        private Key(long channelId, LocalDate localDate, DataQualityMetric dataQualityMetric) {
            this.channelId = channelId;
            this.localDate = localDate;
            this.dataQualityMetric = dataQualityMetric;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return channelId == key.channelId &&
                    Objects.equals(localDate, key.localDate) &&
                    Objects.equals(dataQualityMetric, key.dataQualityMetric);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelId, localDate, dataQualityMetric);
        }
    }

    private Map<Key, LongCounter> calculateFromQuery(ZonedDateTime from, ZonedDateTime to) {
        DeviceDataQualityKpiSqlBuilder sqlBuilder = new DeviceDataQualityKpiSqlBuilder(this.dataValidationKpi.getDeviceGroup(), from.toInstant(), to.toInstant());
        try (Connection connection = this.dataModel.getConnection(false);
             PreparedStatement statement = sqlBuilder.prepare(connection);
             ResultSet resultSet = statement.executeQuery()
        ) {
            return StreamSupport.stream(new ResultSetSpliterator(resultSet), false)
                    .collect(Collectors.toMap(this::toKey, this::toCounter, this::add));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Key toKey(ResultSet resultSet) {
        try {
            long channelId = resultSet.getLong(ResultSetColumn.CHANNELID.index());
            LocalDate localDate = LocalDate.from(ZonedDateTime.ofInstant(resultSet.getTimestamp(ResultSetColumn.READINGTIMESTAMP.index()).toInstant(), clock.getZone()));
            String type = resultSet.getString(ResultSetColumn.READINGQUALITYTYPE.index());
            boolean notSuspect = parseYesNo(resultSet.getString(ResultSetColumn.NOTSUSPECTFLAG.index()));

            ReadingQualityType readingQualityType = new ReadingQualityType(type);

            if (readingQualityType.isSuspect()) {
                return new Key(channelId, localDate, DefaultDataQualityMetric.SUSPECT);
            }
            if (readingQualityType.isMissing()) {
                return new Key(channelId, localDate, DefaultDataQualityMetric.MISSING);
            }
            if (readingQualityType.isConfirmed()) {
                return new Key(channelId, localDate, DefaultDataQualityMetric.CONFIRMED);
            }
            if (readingQualityType.qualityIndex().map(QualityCodeIndex.ESTIMATEGENERIC::equals).orElse(false)) {
                return new Key(channelId, localDate, DefaultDataQualityMetric.ESTIMATED);
            }
            if (qualitiesToValidationRules.containsKey(readingQualityType)) {
                ValidationRule validationRule = qualitiesToValidationRules.get(readingQualityType);
                if (notSuspect) {
                    return new Key(channelId, localDate, DefaultDataQualityMetric.INFORMATIVE);
                } else {
                    return new Key(channelId, localDate, new ValidatorType(validationRule.getImplementation()));
                }
            } else if (qualitiesToEstimationRules.containsKey(readingQualityType)) {
                EstimationRule estimationRule = qualitiesToEstimationRules.get(readingQualityType);
                return new Key(channelId, localDate, new EstimatorType(estimationRule.getImplementation()));
            } else {
                // this should normally not happen
                return new Key(channelId, localDate, DefaultDataQualityMetric.UNKNOWN);
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private LongCounter toCounter(ResultSet resultSet) {
        try {
            LongCounter counter = Counters.newLenientLongCounter();
            counter.add(resultSet.getLong(ResultSetColumn.COUNTER.index()));
            return counter;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private LongCounter add(LongCounter c1, LongCounter c2) {
        c1.add(c2.getValue());
        return c1;
    }

    private Map<ReadingQualityType, ValidationRule> mapQualitiesToValidationRules() {
        return validationService.getValidationRuleSets().stream()
                .filter(ruleSet -> QualityCodeSystem.MDC == ruleSet.getQualityCodeSystem())
                .map(ValidationRuleSet::getRuleSetVersions)
                .flatMap(Collection::stream)
                .map(ValidationRuleSetVersion::getRules)
                .flatMap(Collection::stream)
                .map(IValidationRule.class::cast)
                .filter(rule -> !rule.getReadingQualityType().isMissing())
                .filter(rule -> !rule.getReadingQualityType().isSuspect())
                .collect(Collectors.toMap(IValidationRule::getReadingQualityType, Function.identity()));
    }

    private Map<ReadingQualityType, EstimationRule> mapQualitiesToEstimationRules() {
        return estimationService.getEstimationRuleSets().stream()
                .filter(ruleSet -> QualityCodeSystem.MDC == ruleSet.getQualityCodeSystem())
                .flatMap(ruleSet -> ruleSet.getRules().stream())
                .collect(Collectors.toMap(
                        rule -> ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, Long.valueOf(rule.getId()).intValue()),
                        Function.identity()));
    }

    private Boolean parseYesNo(String value) {
        return "Y".equals(value);
    }
}
