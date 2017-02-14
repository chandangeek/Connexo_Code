/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
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
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.impl.kpi.DeviceDataQualityKpiSqlBuilder.ResultSetColumn;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.elster.jupiter.util.conditions.Where.where;
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
    private List<Validator> validators;
    private List<Estimator> estimators;

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
        validators = validationService.getAvailableValidators(QualityCodeSystem.MDC);
        estimators = estimationService.getAvailableEstimators(QualityCodeSystem.MDC);
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

        Map<DataQualityKpiMemberType, Map<Instant, BigDecimal>> dataMap = new HashMap<>();
        ZonedDateTime calculateDate = start;
        while (!calculateDate.isAfter(end)) {

            Instant calculateInstant = calculateDate.toInstant();

            long registerSuspects = channels.stream()
                    .filter(not(Channel::isRegular))
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long channelSuspects = channels.stream()
                    .filter(Channel::isRegular)
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long totalSuspects = registerSuspects + channelSuspects;

            long informative = channels.stream()
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.INFORMATIVE))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long added = channels.stream()
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.ADDED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long edited = channels.stream()
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.EDITED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long removed = channels.stream()
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.REMOVED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long estimated = channels.stream()
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.ESTIMATED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long confirmed = channels.stream()
                    .map(channelToKey(calculateDate, PredefinedDataQualityMetric.CONFIRMED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.CHANNEL, newMap()).put(calculateInstant, BigDecimal.valueOf(channelSuspects));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.REGISTER, newMap()).put(calculateInstant, BigDecimal.valueOf(registerSuspects));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.SUSPECT, newMap()).put(calculateInstant, BigDecimal.valueOf(totalSuspects));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.INFORMATIVE, newMap()).put(calculateInstant, BigDecimal.valueOf(informative));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.ADDED, newMap()).put(calculateInstant, BigDecimal.valueOf(added));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.EDITED, newMap()).put(calculateInstant, BigDecimal.valueOf(edited));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.REMOVED, newMap()).put(calculateInstant, BigDecimal.valueOf(removed));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.ESTIMATED, newMap()).put(calculateInstant, BigDecimal.valueOf(estimated));
            dataMap.computeIfAbsent(FixedDataQualityKpiMemberType.CONFIRMED, newMap()).put(calculateInstant, BigDecimal.valueOf(confirmed));

            for (Validator validator : validators) {
                long count = channels.stream()
                        .map(channelToKey(calculateDate, new ValidatorDataQualityMetric(validator)))
                        .map(counterMap::get)
                        .filter(Objects::nonNull)
                        .mapToLong(LongCounter::getValue)
                        .sum();
                dataMap.computeIfAbsent(new NamedDataQualityKpiMemberType(validator.getClass().getSimpleName()), newMap())
                        .put(calculateInstant, BigDecimal.valueOf(count));
            }

            for (Estimator estimator : estimators) {
                long count = channels.stream()
                        .map(channelToKey(calculateDate, new EstimatorDataQualityMetric(estimator)))
                        .map(counterMap::get)
                        .filter(Objects::nonNull)
                        .mapToLong(LongCounter::getValue)
                        .sum();
                dataMap.computeIfAbsent(new NamedDataQualityKpiMemberType(estimator.getClass().getSimpleName()), newMap())
                        .put(calculateInstant, BigDecimal.valueOf(count));
            }

            calculateDate = calculateDate.plusDays(1);
        }

        DataValidationKpiChild dataValidationKpiChild = dataValidationKpiChildMap.get(meter.getId());
        Map<KpiMember, Map<Instant, BigDecimal>> memberScores = new HashMap<>();
        dataMap.entrySet().forEach(entry -> {
            KpiMember entryMember = dataValidationKpiChild.getChildKpi()
                    .getMembers()
                    .stream()
                    .filter(kpiMember -> kpiMember.getName().startsWith(entry.getKey().getName().toUpperCase()))
                    .findAny()
                    .get();
            memberScores.put(entryMember, entry.getValue());
        });

        dataValidationKpiChild.getChildKpi().store(memberScores);
    }

    private Function<Channel, Key> channelToKey(ZonedDateTime calculateDate, DataQualityMetric dataQualityMetric) {
        return channel -> new Key(channel.getId(), calculateDate.toLocalDate(), dataQualityMetric);
    }

    private Function<Object, Map<Instant, BigDecimal>> newMap() {
        return any -> new HashMap<>();
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
            Timestamp timestamp = resultSet.getTimestamp(ResultSetColumn.READINGTIMESTAMP.index());
            LocalDate localDate = LocalDate.from(ZonedDateTime.ofInstant(timestamp.toInstant(), clock.getZone()));
            String type = resultSet.getString(ResultSetColumn.READINGQUALITYTYPE.index());
            boolean notSuspect = parseYesNo(resultSet.getString(ResultSetColumn.NOTSUSPECTFLAG.index()));

            ReadingQualityType readingQualityType = new ReadingQualityType(type);

            for (PredefinedDataQualityMetric metric : PredefinedDataQualityMetric.values()) {
                if (metric.accept(readingQualityType)) {
                    return new Key(channelId, localDate, metric);
                }
            }
            if (qualitiesToValidationRules.containsKey(readingQualityType)) {
                ValidationRule validationRule = qualitiesToValidationRules.get(readingQualityType);
                return new Key(channelId, localDate, notSuspect ? PredefinedDataQualityMetric.INFORMATIVE : new ValidatorDataQualityMetric(validationRule.getImplementation()));
            }
            if (qualitiesToEstimationRules.containsKey(readingQualityType)) {
                EstimationRule estimationRule = qualitiesToEstimationRules.get(readingQualityType);
                return new Key(channelId, localDate, new EstimatorDataQualityMetric(estimationRule.getImplementation()));
            }
            // this should normally not happen
            return new Key(channelId, localDate, PredefinedDataQualityMetric.UNKNOWN);
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
        return validationService.getRuleSetQuery()
                .select(where("qualityCodeSystem").isEqualTo(QualityCodeSystem.MDC))
                .stream()
                .map(ValidationRuleSet::getRuleSetVersions)
                .flatMap(Collection::stream)
                .map(ValidationRuleSetVersion::getRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(this::getReadingQualityType, Function.identity()));
    }

    private Map<ReadingQualityType, EstimationRule> mapQualitiesToEstimationRules() {
        return estimationService.getEstimationRuleSetQuery()
                .select(where("qualityCodeSystem").isEqualTo(QualityCodeSystem.MDC))
                .stream()
                .map(EstimationRuleSet::getRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(this::getReadingQualityType, Function.identity()));
    }

    private ReadingQualityType getReadingQualityType(ValidationRule validationRule) {
        return validationRule.getReadingQualityType();
    }

    private ReadingQualityType getReadingQualityType(EstimationRule estimationRule) {
        return ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, Long.valueOf(estimationRule.getId()).intValue());
    }

    private Boolean parseYesNo(String value) {
        return "Y".equals(value);
    }
}
