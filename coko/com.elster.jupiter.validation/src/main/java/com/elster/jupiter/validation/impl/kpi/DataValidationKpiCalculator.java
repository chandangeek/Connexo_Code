/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.LongCounter;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.IValidationRule;

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.streams.Predicates.not;

public class DataValidationKpiCalculator implements DataManagementKpiCalculator {

    private final ValidationService validationService;
    private final DataValidationKpiImpl dataValidationKpi;
    private final Clock clock;
    private final DataModel dataModel;
    private Map<ReadingQualityType, IValidationRule> qualitiesToRules;
    private ZonedDateTime end;
    private ZonedDateTime start;
    private Map<Key, LongCounter> counterMap;
    private Map<Long, DataValidationKpiChild> dataValidationKpiChildMap;

    DataValidationKpiCalculator(ValidationService validationService, DataModel dataModel, DataValidationKpiImpl dataValidationKpi, Clock clock) {
        this.validationService = validationService;
        this.dataModel = dataModel;
        this.dataValidationKpi = dataValidationKpi;
        this.clock = clock;
    }

    @Override
    public void calculate() {
        dataValidationKpiChildMap = dataValidationKpi.updateMembers();
        if (dataValidationKpi.isCancelled()) {
            dataValidationKpi.dropDataValidationKpi();
            return;
        }
        qualitiesToRules = mapQualitiesToRules();
        end = clock.instant().atZone(clock.getZone()).with(LocalTime.MIDNIGHT).plusDays(1);
        start = end.minusMonths(1);
        counterMap = calculateFromQuery(start, end);
    }

    @Override
    public void store(EndDevice endDevice) {
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

        Map<DataValidationKpiMemberTypes, Map<Instant, BigDecimal>> dataMap = new HashMap<>();
        ZonedDateTime calculateDate = start;
        while (!calculateDate.isAfter(end)) {

            Instant calculateInstant = calculateDate.toInstant();
            long registers = channels.stream()
                    .filter(not(Channel::isRegular))
                    .map(channelToKey(calculateDate, SpecialValidatorTypes.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();
            long regular = channels.stream()
                    .filter(Channel::isRegular)
                    .map(channelToKey(calculateDate, SpecialValidatorTypes.SUSPECT))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();
            long total = registers + regular;

            long missing = channels.stream()
                    .map(channelToKey(calculateDate, SpecialValidatorTypes.MISSING))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long threshold = channels.stream()
                    .map(channelToKey(calculateDate, SpecialValidatorTypes.THRESHOLDVALIDATOR))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long registerIncrease = channels.stream()
                    .map(channelToKey(calculateDate, SpecialValidatorTypes.REGISTERINCREASEVALIDATOR))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            long readingQualitiesValidator = channels.stream()
                    .map(channelToKey(calculateDate, SpecialValidatorTypes.READINGQUALITIESVALIDATOR))
                    .map(counterMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(LongCounter::getValue)
                    .sum();

            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.CHANNEL, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(regular));
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.REGISTER, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(registers));
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.SUSPECT, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(total));
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.ALLDATAVALIDATED, type -> new HashMap<>()).put(calculateInstant, BigDecimal.ZERO);
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.CHANNEL, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(regular));
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.MISSINGVALUESVALIDATOR, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(missing));
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.THRESHOLDVALIDATOR, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(threshold));
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.READINGQUALITIESVALIDATOR, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(readingQualitiesValidator));
            dataMap.computeIfAbsent(DataValidationKpiMemberTypes.REGISTERINCREASEVALIDATOR, type -> new HashMap<>()).put(calculateInstant, BigDecimal.valueOf(registerIncrease));

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

    private Function<Channel, Key> channelToKey(ZonedDateTime calculateDate, SpecialValidatorTypes validatorType) {
        return channel -> new Key(channel.getId(), calculateDate.toLocalDate(), validatorType);
    }

    DataValidationKpiImpl getDataValidationKpi() {
        return dataValidationKpi;
    }

    private interface ValidatorType extends HasName {
    }

    private enum SpecialValidatorTypes implements ValidatorType {
        MISSING, SUSPECT, UNKNOWN, THRESHOLDVALIDATOR, READINGQUALITIESVALIDATOR, REGISTERINCREASEVALIDATOR;

        @Override
        public String getName() {
            return name();
        }
    }

    private static final class SimpleValidatorType implements ValidatorType {
        private final String validator;

        public SimpleValidatorType(String validator) {
            this.validator = validator;
        }

        @Override
        public String getName() {
            return validator;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SimpleValidatorType that = (SimpleValidatorType) o;
            return Objects.equals(validator, that.validator);
        }

        @Override
        public int hashCode() {
            return Objects.hash(validator);
        }
    }

    private final static class Key {
        private final long channelId;
        private final LocalDate localDate;
        private final ValidatorType validator;

        private Key(long channelId, LocalDate localDate, ValidatorType validator) {
            this.channelId = channelId;
            this.localDate = localDate;
            this.validator = validator;
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
                    Objects.equals(validator, key.validator);
        }

        @Override
        public int hashCode() {
            return Objects.hash(channelId, localDate, validator);
        }
    }

    private Map<Key, LongCounter> calculateFromQuery(ZonedDateTime from, ZonedDateTime to) {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT q.channelid, TRUNC(utc2date(q.readingtimestamp, t.TIMEZONENAME), 'DDD'), q.type, COUNT(*)" +
                " from MTR_READINGQUALITY q, IDS_TIMESERIES t WHERE" +
                " exists (select id from MTR_CHANNEL c where q.CHANNELID = c.ID and c.TIMESERIESID = t.ID)" +
                " AND q.channelid in (select ID from MTR_CHANNEL" +
                " WHERE CHANNEL_CONTAINER IN (SELECT ID FROM MTR_CHANNEL_CONTAINER " +
                " where METER_ACTIVATION in (select ID from MTR_METERACTIVATION where METERID in (");
        sqlBuilder.add(dataValidationKpi.getDeviceGroup().toSubQuery("ID").toFragment());
        sqlBuilder.append("))))" +
                " AND (q.type IN ('2.5.258', '3.5.258', '2.5.259', '3.5.259') OR q.type LIKE '2.6.%' OR q.type LIKE '3.6.%')" +
                " AND q.actual ='Y'" +
                " AND readingtimestamp  > ");
        sqlBuilder.addLong(from.toInstant().toEpochMilli());
        sqlBuilder.append(" AND readingtimestamp <= ");
        sqlBuilder.addLong(to.toInstant().toEpochMilli());
        sqlBuilder.append(" GROUP BY TRUNC(utc2date(q.readingtimestamp, t.TIMEZONENAME), 'DDD')," +
                " q.type," +
                " q.channelid");
        try (
                Connection connection = dataModel.getConnection(false);
                PreparedStatement statement = sqlBuilder.prepare(connection);
                ResultSet resultSet = statement.executeQuery()
        ) {

            return StreamSupport.stream(new ResultSetSpliterator(resultSet), false)
                    .collect(Collectors.toMap(
                            this::toKey,
                            this::toCounter,
                            (c1, c2) -> {
                                c1.add(c2.getValue());
                                return c1;
                            }
                    ));

        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Key toKey(ResultSet resultSet) {
        try {
            Timestamp sqlDate = resultSet.getTimestamp(2);
            LocalDate localDate = LocalDate.from(ZonedDateTime.ofInstant(sqlDate.toInstant(), clock.getZone()));
            String type = resultSet.getString(3);
            ReadingQualityType readingQualityType = new ReadingQualityType(type);

            if (readingQualityType.isSuspect()) {
                return new Key(resultSet.getLong(1), localDate, SpecialValidatorTypes.SUSPECT);
            }
            if (readingQualityType.isMissing()) {
                return new Key(resultSet.getLong(1), localDate, SpecialValidatorTypes.MISSING);
            }
            IValidationRule validationRule = qualitiesToRules.get(readingQualityType);
            if (validationRule == null) {
                // this should normally not happen
                return new Key(resultSet.getLong(1), localDate, SpecialValidatorTypes.UNKNOWN);
            }
            for (SpecialValidatorTypes validatorType : SpecialValidatorTypes.values()) {
                if (is(validationRule.getImplementation()).containingIgnoringCase(validatorType.name())) {
                    return new Key(resultSet.getLong(1), localDate, validatorType);
                }
            }
            return new Key(resultSet.getLong(1), localDate, new SimpleValidatorType(validationRule.getImplementation()));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private LongCounter toCounter(ResultSet resultSet) {
        try {
            LongCounter counter = Counters.newLenientLongCounter();
            counter.add(resultSet.getLong(4));
            return counter;
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private Map<ReadingQualityType, IValidationRule> mapQualitiesToRules() {
        return validationService.getValidationRuleSets()
                .stream()
                .map(ValidationRuleSet::getRuleSetVersions)
                .flatMap(Collection::stream)
                .map(ValidationRuleSetVersion::getRules)
                .flatMap(Collection::stream)
                .map(IValidationRule.class::cast)
                .filter(rule -> !rule.getReadingQualityType().isMissing())
                .filter(rule -> !rule.getReadingQualityType().isSuspect())
                .collect(Collectors.toMap(
                        IValidationRule::getReadingQualityType,
                        Function.identity()
                ));
    }
}
