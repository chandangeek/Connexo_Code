package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.util.Pair;
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

    private final ReadingTypeRequirement requirement;
    private final ReadingTypeDeliverable deliverable;
    private final List<Channel> matchingChannels;
    private final VirtualReadingType targetReadingType;
    private final Range<Instant> rawDataPeriod;
    private final int meterActivationSequenceNumber;
    private ChannelContract preferredChannel;   // Lazy from the list of matching channels and the targetIntervalLength

    public VirtualReadingTypeRequirement(ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, List<Channel> matchingChannels, VirtualReadingType targetReadingType, MeterActivation meterActivation, Range<Instant> requestedPeriod, int meterActivationSequenceNumber) {
        super();
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.rawDataPeriod = requestedPeriod.intersection(meterActivation.getRange());
        this.matchingChannels = Collections.unmodifiableList(matchingChannels);
        this.targetReadingType = targetReadingType;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
    }

    public List<Channel> getMatchingChannels() {
        return matchingChannels;
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

    private String sqlComment() {
        return this.requirement.getName() + " for " + this.deliverable.getName() + " in " + this.prettyPrintMeterActivationPeriod();
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
        SqlConstants.TimeSeriesColumnNames
                .appendAllAggregatedSelectValues(
                        this.deliverable.getReadingType(),
                        sqlBuilder);
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
        IntervalLength.from(this.deliverable.getReadingType()).appendOracleFormatModelTo(sqlBuilder);
        sqlBuilder.append(")");
    }

    @SuppressWarnings("unchecked")
    private void appendDefinitionWithoutAggregation(SqlBuilder sqlBuilder) {
        sqlBuilder.add(
                this.getPreferredChannel()
                        .getTimeSeries()
                        .getRawValuesSql(
                                this.rawDataPeriod,
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS),
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.VALUE),
                                this.toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames.LOCALDATE)));
    }

    private Pair<String, String> toFieldSpecAndAliasNamePair(SqlConstants.TimeSeriesColumnNames columnName) {
        return Pair.of(columnName.fieldSpecName(), columnName.sqlName());
    }

    private ChannelContract getPreferredChannel() {
        if (this.preferredChannel == null) {
            this.preferredChannel = this.findPreferredChannel();
        }
        return this.preferredChannel;
    }

    private ChannelContract findPreferredChannel() {
        return new MatchingChannelSelector(this.matchingChannels)
                    .getPreferredChannel(this.targetReadingType)
                    .map(ChannelContract.class::cast)
                    .orElseThrow(() -> new IllegalStateException("Calculation of preferred channel failed before"));
    }

    private boolean aggregationIsRequired() {
        return IntervalLength.from(this.getPreferredChannel().getMainReadingType()) != IntervalLength.from(this.deliverable.getReadingType());
    }

    void appendReferenceTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append(".");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
    }

}