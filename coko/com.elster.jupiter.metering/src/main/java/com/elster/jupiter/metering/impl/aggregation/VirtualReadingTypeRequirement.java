package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;

import java.time.Instant;
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
public class VirtualReadingTypeRequirement {

    private final Formula.Mode mode;
    private final ReadingTypeRequirement requirement;
    private final ReadingTypeDeliverable deliverable;
    private final List<Channel> matchingChannels;
    private VirtualReadingType targetReadingType;
    private final Range<Instant> rawDataPeriod;
    private final int meterActivationSequenceNumber;
    private Optional<ChannelContract> preferredChannel;   // Lazy from the list of matching channels and the targetIntervalLength

    public VirtualReadingTypeRequirement(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, List<Channel> matchingChannels, VirtualReadingType targetReadingType, MeterActivation meterActivation, Range<Instant> requestedPeriod, int meterActivationSequenceNumber) {
        super();
        this.mode = mode;
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.rawDataPeriod = this.toOpenClosed(requestedPeriod.intersection(meterActivation.getRange()));
        this.matchingChannels = Collections.unmodifiableList(matchingChannels);
        this.targetReadingType = targetReadingType;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
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
    String sqlName () {
        return "rid" + this.requirement.getId() + "_" + this.deliverable.getId() + "_" + this.meterActivationSequenceNumber;
    }

    String tempSqlName () {
        return "temp" + this.requirement.getId() + "_" + this.deliverable.getId() + "_" + this.meterActivationSequenceNumber;
    }

    private String sqlComment() {
        return this.requirement.getName() + this.prettyPrintedReadingType() + " for " + this.deliverable.getName() + " in " + this.prettyPrintMeterActivationPeriod();
    }

    private String prettyPrintedReadingType() {
        return " as " + this.readingTypeForSqlComment();
    }

    private String readingTypeForSqlComment() {
        if (this.requirement instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement requirement = (FullySpecifiedReadingTypeRequirement) this.requirement;
            return requirement.getReadingType().getMRID();
        } else {
            PartiallySpecifiedReadingTypeRequirement requirement = (PartiallySpecifiedReadingTypeRequirement) this.requirement;
            return requirement.getReadingTypeTemplate().toString();
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
        sqlBuilder.append("  FROM (");
        sqlBuilder.add(
                this.getPreferredChannel()
                        .getTimeSeries()
                        .getRawValuesSql(
                                this.rawDataPeriod,
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS),
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.VALUE),
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.LOCALDATE)));
        sqlBuilder.append(") rawdata GROUP BY TRUNC(");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName());
        sqlBuilder.append(", ");
        this.targetReadingType.getIntervalLength().appendOracleFormatModelTo(sqlBuilder);
        sqlBuilder.append(")");
    }

    @SuppressWarnings("unchecked")
    private void appendDefinitionWithoutAggregation(SqlBuilder sqlBuilder) {

        sqlBuilder.append("select ");

        sqlBuilder.append("timeseriesid , utcstamp , versioncount , recordtime, (select max(case when type like '%.5.258' then 4 when type like '%.5.259' then 3 else 1 end) from mtr_readingquality where readingtype = '");
        sqlBuilder.append(this.getPreferredChannel().getMainReadingType().getMRID());
        sqlBuilder.append("' and readingtimestamp = UTCSTAMP and channelid = ");
        sqlBuilder.append("" + this.getPreferredChannel().getId());
        sqlBuilder.append(" and (type like '%.5.258' or type like '%.5.259' or type like '%.7.%' or type like '%.8.%')) AS processStatus, value, localdate");

        sqlBuilder.append(" from(");
        sqlBuilder.add(
                this.getPreferredChannel()
                        .getTimeSeries()
                        .getRawValuesSql(
                                this.rawDataPeriod,
                                //this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS),
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.VALUE),
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.LOCALDATE)));
        sqlBuilder.append(") ");
        sqlBuilder.append(tempSqlName());
    }

    private Pair<String, String> toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames columnName) {
        return Pair.of(columnName.fieldSpecName(), columnName.sqlName());
    }

    ChannelContract getPreferredChannel() {
        if (this.preferredChannel == null) {
            this.preferredChannel = this.findPreferredChannel();
        }
        return this.preferredChannel.orElseThrow(() -> new IllegalStateException("Calculation of preferred channel failed before"));
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

}