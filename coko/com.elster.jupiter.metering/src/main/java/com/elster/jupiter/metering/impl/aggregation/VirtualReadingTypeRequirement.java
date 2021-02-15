/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.TimeOfUseBucketInconsitencyException;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Models a {@link ReadingTypeRequirement} in the context of
 * one {@link MeterActivation} of the usage point.
 * It contains the {@link Channel}s of the related meter
 * that match the ReadingTypeRequirement.
 * <p>
 * It is capable of selecting the most appropriate Channel
 * to produce values of the target reading type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (15:24)
 */
@LiteralSql
class VirtualReadingTypeRequirement {

    private final Thesaurus thesaurus;
    private final Formula.Mode mode;
    private final ReadingTypeRequirement requirement;
    private final ReadingTypeDeliverable deliverable;
    private final Calendar calendar;
    private final List<Channel> matchingChannels;
    private VirtualReadingType targetReadingType;
    private final Range<Instant> rawDataPeriod;
    private final int meterActivationSequenceNumber;
    private final UsagePoint usagePoint;
    private Optional<ChannelContract> preferredChannel;   // Lazy from the list of matching channels and the targetIntervalLength

    VirtualReadingTypeRequirement(Thesaurus thesaurus, Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, List<Channel> matchingChannels, VirtualReadingType targetReadingType, MeterActivationSet meterActivationSet, Range<Instant> requestedPeriod, int meterActivationSequenceNumber) {
        super();
        this.thesaurus = thesaurus;
        this.mode = mode;
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.calendar = meterActivationSet.getCalendar();
        this.rawDataPeriod = this.toOpenClosed(requestedPeriod.intersection(meterActivationSet.getRange()));
        this.matchingChannels = Collections.unmodifiableList(matchingChannels);
        this.targetReadingType = targetReadingType;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
        this.usagePoint = meterActivationSet.getUsagePoint();
    }

    private Range<Instant> toOpenClosed(Range<Instant> period) {
        return Ranges.copy(period).asOpenClosed();
    }

    /**
     * Returns the String that should be used in SQL statements to refer
     * to the data produced by this VirtualReadingTypeRequirement.
     *
     * @return The id for SQL statements
     */
    String sqlName() {
        return "rid" + this.requirement.getId() + "_" + this.deliverable.getId() + "_" + this.meterActivationSequenceNumber;
    }

    private String tempSqlName() {
        return "temp" + this.requirement.getId() + "_" + this.deliverable.getId() + "_" + this.meterActivationSequenceNumber;
    }

    private String sqlComment() {
        return this.requirement.getName() + this.prettyPrintedReadingType() + " for " + this.deliverable.getName() + " in " + this.prettyPrintMeterActivationPeriod();
    }

    private String prettyPrintedReadingType() {
        return " (" + this.readingTypeForSqlComment() + ")";
    }

    private String readingTypeForSqlComment() {
        if (this.requirement instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement requirement = (FullySpecifiedReadingTypeRequirement) this.requirement;
            return requirement.getReadingType().getMRID();
        } else {
            PartiallySpecifiedReadingTypeRequirement requirement = (PartiallySpecifiedReadingTypeRequirement) this.requirement;
            return requirement.getReadingTypeTemplate().attributeValues();
        }
    }

    private String prettyPrintMeterActivationPeriod() {
        return this.rawDataPeriod.toString();
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(sqlComment()), SqlConstants.TimeSeriesColumnNames.names());
        if (this.aggregationIsRequired()) {
            this.appendDefinitionWithAggregation(withClauseBuilder);
        } else {
            this.appendDefinitionWithoutAggregation(withClauseBuilder);
        }
    }

    @SuppressWarnings("unchecked")
    private void appendDefinitionWithAggregation(SqlBuilder sqlBuilder) {
        sqlBuilder.append("SELECT ");
        VirtualReadingType sourceReadingType = this.getSourceReadingType();
        SqlConstants.TimeSeriesColumnNames.appendAllAggregatedSelectValues(sourceReadingType, this.targetReadingType, sqlBuilder);
        sqlBuilder.append("  FROM (SELECT ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.ID.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VERSIONCOUNT.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.RECORDTIME.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.READINGQUALITY.sqlName());
        sqlBuilder.append(", ");
        sqlBuilder.append(this.sourceChannelValue());
        sqlBuilder.append(" AS ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS.sqlName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VALUE.fieldSpecName());
        addMultiplier(sqlBuilder);
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.LOCALDATE.fieldSpecName());
        sqlBuilder.append(" FROM (");
        this.appendPreferredChannel(sqlBuilder);
        sqlBuilder.append(") rawdata) GROUP BY ");
        this.targetReadingType.getIntervalLength().appendTruncation(sqlBuilder, "rawdata");
    }

    @SuppressWarnings("unchecked")
    private void appendPreferredChannel(SqlBuilder sqlBuilder) {
        ChannelContract preferredChannel = this.getPreferredChannel();
        int requestedTimeOfUseBucket = this.targetReadingType.getTimeOfUseBucket();
        int providedTimeOfUseBucket = preferredChannel.getMainReadingType().getTou();
        if (providedTimeOfUseBucket == requestedTimeOfUseBucket) {
            // Note that this also supports the case where time of use is not requested (i.e. both are zero)
            sqlBuilder.add(this.timeSeriesWithReadingQualitiesSqlBuilder(this.getPreferredChannel().getTimeSeries()));
        } else if (requestedTimeOfUseBucket != 0 && providedTimeOfUseBucket == 0) {
            // Requested but not provided so use calendar at the appropriate interval
            if (this.calendar == null) {
                String errorMessage = "Deliverable (name=" + this.deliverable.getName() + ", id=" + this.deliverable.getId() + ") requires time of use bucket " + requestedTimeOfUseBucket + " but no calendar is configured on the usage point. Validation of linking metrology configuration failed before!";
                Loggers.SQL.severe(() -> errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            sqlBuilder.add(
                    this.calendar
                            .toTimeSeries(
                                    this.targetReadingType.getIntervalLength().toTemporalAmount(),
                                    preferredChannel.getZoneId())
                            .joinSql(
                                    this.timeSeriesWithReadingQualitiesSqlBuilder(preferredChannel.getTimeSeries()),
                                    "ts",
                                    new EventFromReadingType(this.targetReadingType),
                                    this.rawDataPeriod));
        } else {
            /* One of the following erroneous conditions:
             * 1. Not requested but time of use bucket is provided
             * 2. Requested but different time of use bucket is provided
             */
            Loggers.SQL.severe(() -> "Inconsistency between time of use bucket requested by deliverable (name=" + this.deliverable.getName() + ", id=" + this.deliverable.getId() + ") and time of use bucket provided by the preferred channel. Requested " + requestedTimeOfUseBucket + " but got " + providedTimeOfUseBucket);
            throw new TimeOfUseBucketInconsitencyException(this.thesaurus, requestedTimeOfUseBucket, providedTimeOfUseBucket, this.deliverable, this.usagePoint, this.rawDataPeriod);
        }
    }

    Optional<Calendar> getCalendar() {
        ChannelContract preferredChannel = this.getPreferredChannel();
        int requestedTimeOfUseBucket = this.targetReadingType.getTimeOfUseBucket();
        int providedTimeOfUseBucket = preferredChannel.getMainReadingType().getTou();
        if (providedTimeOfUseBucket == requestedTimeOfUseBucket) {
            // Note that this also supports the case where time of use is not requested (i.e. both are zero)
            return Optional.empty();
        } else if (requestedTimeOfUseBucket != 0 && providedTimeOfUseBucket == 0) {
            return Optional.ofNullable(this.calendar);
        } else {
            /* One of the following erroneous conditions:
             * 1. Not requested but time of use bucket is provided
             * 2. Requested but different time of use bucket is provided
             */
            return Optional.empty();
        }
    }

    private SqlBuilder timeSeriesWithReadingQualitiesSqlBuilder(TimeSeries timeSeries) {
        return this.timeSeriesWithReadingQualitiesSqlBuilder(
                timeSeries,
                timeSeries.getRawValuesSql(
                        this.rawDataPeriod,
                        this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.VALUE),
                        this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.LOCALDATE)),
                true);
    }

    private SqlBuilder timeSeriesWithReadingQualitiesSqlBuilder(TimeSeries timeSeries, SqlFragment timeSeriesSql, boolean supportLocalDate) {
        SqlBuilder builder = new SqlBuilder("SELECT NVL(rawts.");
        builder.append(SqlConstants.TimeSeriesColumnNames.ID.fieldSpecName());
        builder.append(", ");
        builder.append(Long.toString(timeSeries.getId()));
        builder.append(") as ");
        builder.append(SqlConstants.TimeSeriesColumnNames.ID.fieldSpecName());
        builder.append(", NVL(rawts.");
        builder.append(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.fieldSpecName());
        builder.append(", rq.readingtimestamp) as ");
        builder.append(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.fieldSpecName());
        builder.append(", rawts.");
        builder.append(SqlConstants.TimeSeriesColumnNames.VERSIONCOUNT.fieldSpecName());
        builder.append(", rawts.");
        builder.append(SqlConstants.TimeSeriesColumnNames.RECORDTIME.fieldSpecName());
        if (supportLocalDate) {
            builder.append(", rawts.");
            builder.append(SqlConstants.TimeSeriesColumnNames.LOCALDATE.fieldSpecName());
        }
        builder.append(", rawts.");
        builder.append(SqlConstants.TimeSeriesColumnNames.VALUE.fieldSpecName());
        builder.append(", NVL(rq.");
        builder.append(SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
        builder.append(", 0) as ");
        builder.append(SqlConstants.TimeSeriesColumnNames.READINGQUALITY.sqlName());
        builder.append(" FROM (");
        builder.add(timeSeriesSql);
        builder.append(") rawts");
        this.appendAggregatedReadingQuality(builder);
        return builder;
    }

    @SuppressWarnings("unchecked")
    private void appendDefinitionWithoutAggregation(SqlBuilder sqlBuilder) {
        sqlBuilder.append("SELECT ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.ID.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VERSIONCOUNT.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.RECORDTIME.fieldSpecName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.READINGQUALITY.sqlName());
        sqlBuilder.append(", '");
        sqlBuilder.append(Long.toString(this.getPreferredChannel().getId()));
        sqlBuilder.append("' AS ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.SOURCECHANNELS.sqlName());
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
        addMultiplier(sqlBuilder);
        sqlBuilder.append(", ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName());
        sqlBuilder.append(" FROM(");
        TimeSeries timeSeries = this.getPreferredChannel().getTimeSeries();
        if (timeSeries.isRegular()) {
            this.appendPreferredChannel(sqlBuilder);
        } else {
            this.appendDefinitionWithoutLocalDate(sqlBuilder, timeSeries);
        }
        sqlBuilder.append(") ");
        sqlBuilder.append(tempSqlName());
    }


    private void addMultiplier(SqlBuilder sqlBuilder) {
        this.getPreferredChannel().getChannelsContainer().getDefaultMultiplier().ifPresent(multiplier -> {
            sqlBuilder.append(" * ");
            sqlBuilder.addObject(multiplier);
        });
    }

    private void appendAggregatedReadingQuality(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" FULL OUTER JOIN (SELECT readingtimestamp, MAX(CASE");
        sqlBuilder.append(" WHEN TYPE LIKE '%.5.258' THEN " + CalculatedReadingRecordImpl.SUSPECT);
        sqlBuilder.append(" WHEN TYPE LIKE '%.5.259' THEN " + CalculatedReadingRecordImpl.MISSING);
        sqlBuilder.append(" ELSE " + CalculatedReadingRecordImpl.ESTIMATED_EDITED);
        sqlBuilder.append(" END) AS ");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
        sqlBuilder.append(" FROM mtr_readingquality WHERE readingtypeid = ");
        sqlBuilder.addLong(((IReadingType) this.getPreferredChannel().getMainReadingType()).getId());
        sqlBuilder.append(" AND channelid = ");
        sqlBuilder.addLong(this.getPreferredChannel().getId());
        sqlBuilder.append(" AND (TYPE LIKE '%.5.258' OR TYPE LIKE '%.5.259' OR TYPE LIKE '%.7.%' OR TYPE LIKE '%.8.%')");
        if (rawDataPeriod.hasLowerBound()) {
            sqlBuilder.append(" AND readingtimestamp > ");
            sqlBuilder.addLong(this.rawDataPeriod.lowerEndpoint().toEpochMilli());
        }
        if (rawDataPeriod.hasUpperBound()) {
            sqlBuilder.append(" AND readingtimestamp <= ");
            sqlBuilder.addLong(this.rawDataPeriod.upperEndpoint().toEpochMilli());
        }
        sqlBuilder.append(" GROUP BY readingtimestamp) rq");
        sqlBuilder.append(" ON rawts.utcstamp = rq.readingtimestamp");
    }

    @SuppressWarnings("unchecked")
    private void appendDefinitionWithoutLocalDate(SqlBuilder sqlBuilder, TimeSeries timeSeries) {
        /* Will use 'sysdate' as value for the localdate column expected by the with clause.
         * Tried to find a 'standard' oracle function that converts UTC milliseconds
         * in a number field to an oracle DATE type but failed.
         * As we do not plan to support time based aggregation on register data
         * it does not really matter if the value of the localdate column
         * is not actually the local version of the register reading's timestamp. */
        sqlBuilder.append("SELECT ts.*, sysdate as " + SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName() + " FROM (");
        sqlBuilder.add(
                this.timeSeriesWithReadingQualitiesSqlBuilder(
                        timeSeries,
                        timeSeries.getRawValuesSql(
                                this.rawDataPeriod,
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.VALUE)),
                        false));
        sqlBuilder.append(") ts");
    }

    private Pair<String, String> toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames columnName) {
        return Pair.of(columnName.fieldSpecName(), columnName.sqlName());
    }

    ChannelContract getPreferredChannel() {
        if ((this.preferredChannel == null) || (!this.preferredChannel.isPresent())) {
            this.preferredChannel = this.findPreferredChannel();
        }
        return this.preferredChannel.orElseThrow(() -> new RequirementNotBackedByMeter(this.thesaurus, this.getTargetReadingType().toString()));
    }

    private Optional<ChannelContract> findPreferredChannel() {
        return new MatchingChannelSelector(this.matchingChannels, this.mode)
                .getPreferredChannel(this.targetReadingType)
                .map(ChannelContract.class::cast);
    }

    private boolean aggregationIsRequired() {
        return Formula.Mode.AUTO.equals(this.mode)
                && IntervalLength.from(this.getPreferredChannel().getMainReadingType()) != this.targetReadingType.getIntervalLength();
    }

    void appendSimpleReferenceTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
    }

    void appendReferenceTo(SqlBuilder sqlBuilder) {
        VirtualReadingType sourceReadingType = this.getSourceReadingType();
        sqlBuilder.append(
                sourceReadingType.buildSqlUnitConversion(
                        this.mode,
                        this.sqlName() + "." + SqlConstants.TimeSeriesColumnNames.VALUE.sqlName(),
                        this.targetReadingType));
    }

    String sourceChannelValue() {
        return "'" + Long.toString(this.getPreferredChannel().getId()) + "'";
    }

    VirtualReadingType getSourceReadingType() {
        return VirtualReadingType.from(this.getPreferredChannel().getMainReadingType());
    }

    VirtualReadingType getTargetReadingType() {
        return targetReadingType;
    }

    void setTargetReadingType(VirtualReadingType targetReadingType) {
        this.targetReadingType = targetReadingType;
        this.findPreferredChannel().ifPresent(this::setPreferredChannel);
    }

    private void setPreferredChannel(ChannelContract preferredChannel) {
        this.preferredChannel = Optional.of(preferredChannel);
    }

    public ServerDataAggregationService.DetailedCalendarUsage toDetailedCalendarUsage() {
        return new DetailedCalendarUsageImpl(
                this.calendar,
                this.targetReadingType.getIntervalLength(),
                this.getPreferredChannel().getZoneId());
    }

    private static class EventFromReadingType implements Event {
        private final VirtualReadingType readingType;

        private EventFromReadingType(VirtualReadingType readingType) {
            this.readingType = readingType;
        }

        @Override
        public long getCode() {
            return this.readingType.getTimeOfUseBucket();
        }

        @Override
        public Instant getCreateTime() {
            return Instant.now();
        }

        @Override
        public long getVersion() {
            return 0;
        }

        @Override
        public Instant getModTime() {
            return this.getCreateTime();
        }

        @Override
        public String getUserName() {
            return "DataAggregationService";
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public String getName() {
            return this.readingType.toString();
        }
    }

    private class DetailedCalendarUsageImpl implements ServerDataAggregationService.DetailedCalendarUsage {
        private final Calendar calendar;
        private final IntervalLength intervalLength;
        private final ZoneId zoneId;

        private DetailedCalendarUsageImpl(Calendar calendar, IntervalLength intervalLength, ZoneId zoneId) {
            this.calendar = calendar;
            this.intervalLength = intervalLength;
            this.zoneId = zoneId;
        }

        @Override
        public Optional<Calendar> getCalendar() {
            return Optional.ofNullable(this.calendar);
        }

        @Override
        public IntervalLength getIntervalLength() {
            return intervalLength;
        }

        @Override
        public ZoneId getZoneId() {
            return zoneId;
        }
    }

}