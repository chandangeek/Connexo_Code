/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.dataquality.impl.DataQualityKpiImpl;
import com.elster.jupiter.dataquality.impl.DataQualityKpiMember;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.LongCounter;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.Predicates;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import com.google.common.collect.ImmutableSet;
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
import java.time.Period;
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

abstract class AbstractDataQualityKpiCalculator implements DataQualityKpiCalculator {

    private static final Period PERIOD_TO_CALCULATE = Period.ofMonths(3);

    private final TransactionService transactionService;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final DataModel dataModel;
    private final Clock clock;
    private final Logger logger;

    private Map<ReadingQualityType, ValidationRule> qualitiesToValidationRules;
    private Map<ReadingQualityType, EstimationRule> qualitiesToEstimationRules;
    private List<Validator> validators;
    private List<Estimator> estimators;

    private DataQualityKpiImpl dataQualityKpi;

    private ZonedDateTime start;
    private ZonedDateTime end;
    private Map<Key, LongCounter> counterMap;
    private Map<Long, DataQualityKpiMember> dataQualityKpiChildMap;

    AbstractDataQualityKpiCalculator(DataQualityServiceProvider serviceProvider, DataQualityKpiImpl dataQualityKpi, Logger logger) {
        this.transactionService = serviceProvider.transactionService();
        this.validationService = serviceProvider.validationService();
        this.estimationService = serviceProvider.estimationService();
        this.dataModel = serviceProvider.dataModel();
        this.clock = serviceProvider.clock();
        this.dataQualityKpi = dataQualityKpi;
        this.logger = logger;
    }

    TransactionService getTransactionService() {
        return transactionService;
    }

    Clock getClock() {
        return clock;
    }

    Logger getLogger() {
        return logger;
    }

    abstract QualityCodeSystem getQualityCodeSystem();

    abstract DataQualityKpiSqlBuilder sqlBuilder();

    void calculateInTransaction() {
        try {
            transactionService.run(this::calculate);
        } catch (Exception ex) {
            transactionService.run(() -> logger.log(Level.WARNING, "Failed to calculate data quality KPI. Error: " + ex.getLocalizedMessage(), ex));
        }
    }

    private void calculate() {
        end = now().with(LocalTime.MIDNIGHT).plusDays(1);
        start = end.minus(PERIOD_TO_CALCULATE);
        dataQualityKpiChildMap = dataQualityKpi.updateMembers(Range.openClosed(start.toInstant(), end.toInstant()));
        if (dataQualityKpi.isCancelled()) {
            return;
        }
        qualitiesToValidationRules = mapReadingQualitiesToValidationRules();
        qualitiesToEstimationRules = mapReadingQualitiesToEstimationRules();
        validators = validationService.getAvailableValidators(getQualityCodeSystem());
        estimators = estimationService.getAvailableEstimators(getQualityCodeSystem());
        counterMap = calculateFromQuery(start, end);
    }

    Instant getStart() {
        return start.toInstant();
    }

    Instant getEnd() {
        return end.toInstant();
    }

    private Map<ReadingQualityType, ValidationRule> mapReadingQualitiesToValidationRules() {
        return validationService.getRuleSetQuery()
                .select(Where.where("qualityCodeSystem").isEqualTo(getQualityCodeSystem()))
                .stream()
                .map(ValidationRuleSet::getRuleSetVersions)
                .flatMap(Collection::stream)
                .map(ValidationRuleSetVersion::getRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(this::getReadingQualityType, Function.identity(), (r1, r2) -> r1));
    }

    private Map<ReadingQualityType, EstimationRule> mapReadingQualitiesToEstimationRules() {
        return estimationService.getEstimationRuleSetQuery()
                .select(Where.where("qualityCodeSystem").isEqualTo(getQualityCodeSystem()))
                .stream()
                .map(EstimationRuleSet::getRules)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(this::getReadingQualityType, Function.identity(), (r1, r2) -> r1));
    }

    private ReadingQualityType getReadingQualityType(ValidationRule validationRule) {
        return validationRule.getReadingQualityType();
    }

    private ReadingQualityType getReadingQualityType(EstimationRule estimationRule) {
        return ReadingQualityType.of(getQualityCodeSystem(), QualityCodeCategory.ESTIMATED, Long.valueOf(estimationRule.getId()).intValue());
    }

    private ZonedDateTime now() {
        return this.clock.instant().atZone(this.clock.getZone());
    }

    private Map<Key, LongCounter> calculateFromQuery(ZonedDateTime from, ZonedDateTime to) {
        DataQualityKpiSqlBuilder sqlBuilder = sqlBuilder().init(from.toInstant(), to.toInstant());
        try (Connection connection = this.dataModel.getConnection(false);
             PreparedStatement statement = sqlBuilder.prepare(connection);
             ResultSet resultSet = statement.executeQuery()
        ) {
            return StreamSupport.stream(new ResultSetSpliterator(resultSet), false)
                    .flatMap(rs -> toKeys(rs).stream().map(key -> Pair.of(key, toCounter(rs))))
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getLast, this::add));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Set<Key> toKeys(ResultSet resultSet) {
        try {
            long channelId = resultSet.getLong(DataQualityKpiSqlBuilder.ResultSetColumn.CHANNELID.index());
            Timestamp timestamp = resultSet.getTimestamp(DataQualityKpiSqlBuilder.ResultSetColumn.READINGTIMESTAMP.index());
            LocalDate localDate = LocalDate.from(ZonedDateTime.ofInstant(timestamp.toInstant(), clock.getZone()));
            String type = resultSet.getString(DataQualityKpiSqlBuilder.ResultSetColumn.READINGQUALITYTYPE.index());
            boolean notSuspect = parseYesNo(resultSet.getString(DataQualityKpiSqlBuilder.ResultSetColumn.NOTSUSPECTFLAG.index()));

            ReadingQualityType readingQualityType = new ReadingQualityType(type);

            for (DataQualityMetric.PredefinedDataQualityMetric metric : DataQualityMetric.PredefinedDataQualityMetric.values()) {
                if (metric.accept(readingQualityType)) {
                    return ImmutableSet.of(new Key(channelId, localDate, metric));
                }
            }
            if (qualitiesToValidationRules.containsKey(readingQualityType)) {
                ValidationRule validationRule = qualitiesToValidationRules.get(readingQualityType);
                return ImmutableSet.of(new Key(channelId, localDate,
                        notSuspect ? DataQualityMetric.PredefinedDataQualityMetric.INFORMATIVE : new DataQualityMetric.ValidatorDataQualityMetric(validationRule)));
            }
            if (qualitiesToEstimationRules.containsKey(readingQualityType)) {
                EstimationRule estimationRule = qualitiesToEstimationRules.get(readingQualityType);
                return ImmutableSet.of(
                        new Key(channelId, localDate, DataQualityMetric.PredefinedDataQualityMetric.ESTIMATED),
                        new Key(channelId, localDate, new DataQualityMetric.EstimatorDataQualityMetric(estimationRule))
                );
            }
            // this should normally not happen
            return ImmutableSet.of(new Key(channelId, localDate, DataQualityMetric.PredefinedDataQualityMetric.UNKNOWN));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private LongCounter toCounter(ResultSet resultSet) {
        try {
            LongCounter counter = Counters.newLenientLongCounter();
            counter.add(resultSet.getLong(DataQualityKpiSqlBuilder.ResultSetColumn.COUNTER.index()));
            return counter;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private LongCounter add(LongCounter c1, LongCounter c2) {
        c1.add(c2.getValue());
        return c1;
    }

    private Boolean parseYesNo(String value) {
        return "Y".equals(value);
    }

    private Map<DataQualityKpiMemberType, Map<Instant, BigDecimal>> prepareDataMap(Collection<Channel> channels) {
        Map<DataQualityKpiMemberType, Map<Instant, BigDecimal>> dataMap = new HashMap<>();
        ZonedDateTime calculateDate = this.start;
        while (!calculateDate.isAfter(this.end)) {

            Instant calculateInstant = calculateDate.toInstant();

            long registerSuspects = channels.stream()
                    .filter(Predicates.not(Channel::isRegular))
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long channelSuspects = channels.stream()
                    .filter(Channel::isRegular)
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long totalSuspects = registerSuspects + channelSuspects;

            long informative = channels.stream()
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.INFORMATIVE))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long added = channels.stream()
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.ADDED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long edited = channels.stream()
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.EDITED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long removed = channels.stream()
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.REMOVED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long estimated = channels.stream()
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.ESTIMATED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long confirmed = channels.stream()
                    .map(channelToKey(calculateDate, DataQualityMetric.PredefinedDataQualityMetric.CONFIRMED))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.CHANNEL, newMap()).put(calculateInstant, BigDecimal.valueOf(channelSuspects));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.REGISTER, newMap()).put(calculateInstant, BigDecimal.valueOf(registerSuspects));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.SUSPECT, newMap()).put(calculateInstant, BigDecimal.valueOf(totalSuspects));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.INFORMATIVE, newMap()).put(calculateInstant, BigDecimal.valueOf(informative));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.ADDED, newMap()).put(calculateInstant, BigDecimal.valueOf(added));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.EDITED, newMap()).put(calculateInstant, BigDecimal.valueOf(edited));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.REMOVED, newMap()).put(calculateInstant, BigDecimal.valueOf(removed));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.ESTIMATED, newMap()).put(calculateInstant, BigDecimal.valueOf(estimated));
            dataMap.computeIfAbsent(DataQualityKpiMemberType.PredefinedKpiMemberType.CONFIRMED, newMap()).put(calculateInstant, BigDecimal.valueOf(confirmed));

            for (Validator validator : validators) {
                long amount = channels.stream()
                        .map(channelToKey(calculateDate, new DataQualityMetric.ValidatorDataQualityMetric(validator)))
                        .map(counterMap::get)
                        .filter(Objects::nonNull)
                        .mapToLong(LongCounter::getValue)
                        .sum();
                dataMap.computeIfAbsent(new DataQualityKpiMemberType.ValidatorKpiMemberType(validator), newMap()).put(calculateInstant, BigDecimal.valueOf(amount));
            }

            for (Estimator estimator : estimators) {
                long amount = channels.stream()
                        .map(channelToKey(calculateDate, new DataQualityMetric.EstimatorDataQualityMetric(estimator)))
                        .map(counterMap::get)
                        .filter(Objects::nonNull)
                        .mapToLong(LongCounter::getValue)
                        .sum();
                dataMap.computeIfAbsent(new DataQualityKpiMemberType.EstimatorKpiMemberType(estimator), newMap()).put(calculateInstant, BigDecimal.valueOf(amount));
            }
            calculateDate = calculateDate.plusDays(1);
        }
        return dataMap;
    }

    void storeForChannels(Long objectId, String kpiMemberNameSuffix, Collection<Channel> channels) {
        Kpi kpi = dataQualityKpiChildMap.get(objectId).getChildKpi();

        Map<KpiMember, Map<Instant, BigDecimal>> memberScores = new HashMap<>();

        prepareDataMap(channels).entrySet().forEach(entry -> {
            String kpiMemberName = entry.getKey().getName() + DataQualityKpiMember.KPIMEMBERNAME_SEPARATOR + kpiMemberNameSuffix;
            KpiMember entryMember = kpi
                    .getMembers()
                    .stream()
                    .filter(kpiMember -> kpiMember.getName().equals(kpiMemberName))
                    .findAny()
                    .get();
            memberScores.put(entryMember, entry.getValue());
        });

        kpi.store(memberScores);
    }

    private Function<Channel, Key> channelToKey(ZonedDateTime calculateDate, DataQualityMetric dataQualityMetric) {
        return channel -> new Key(channel.getId(), calculateDate.toLocalDate(), dataQualityMetric);
    }

    private Function<Object, Map<Instant, BigDecimal>> newMap() {
        return any -> new HashMap<>();
    }

    final static class Key {

        private final long channelId;
        private final LocalDate localDate;
        private final DataQualityMetric dataQualityMetric;

        Key(long channelId, LocalDate localDate, DataQualityMetric dataQualityMetric) {
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

}
