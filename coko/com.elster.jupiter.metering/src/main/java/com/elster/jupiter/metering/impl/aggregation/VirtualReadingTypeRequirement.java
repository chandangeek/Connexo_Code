package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.util.sql.SqlBuilder;

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
    private final IntervalLength targetIntervalLength;
    private final MeterActivation meterActivation;
    private final int meterActivationSequenceNumber;
    // Todo: Will become valid usage once moved to the metering bundle
    private ChannelContract preferredChannel;   // Lazy from the list of matching channels and the targetIntervalLength

    public VirtualReadingTypeRequirement(ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, List<Channel> matchingChannels, IntervalLength targetIntervalLength, MeterActivation meterActivation, int meterActivationSequenceNumber) {
        super();
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.matchingChannels = Collections.unmodifiableList(matchingChannels);
        this.targetIntervalLength = targetIntervalLength;
        this.meterActivation = meterActivation;
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
        return this.meterActivation.getRange().toString();
    }

    void appendDefinitionTo(ClauseAwareSqlBuilder sqlBuilder) {
        SqlBuilder withClauseBuilder = sqlBuilder.with(this.sqlName(), Optional.of(sqlComment()), SqlConstants.TimeSeriesColumnNames.names());
        // Todo: clip the MeterActivation's range to the requested period
        withClauseBuilder.add(
                this.getPreferredChannel()
                        .getTimeSeries()
                        .getRawValuesSql(
                                this.meterActivation.getRange(),
                                SqlConstants.TimeSeriesColumnNames.PROCESSSTATUS.fieldSpecName(),
                                SqlConstants.TimeSeriesColumnNames.VALUE.fieldSpecName()));
    }

    private ChannelContract getPreferredChannel() {
        if (this.preferredChannel == null) {
            this.preferredChannel = this.findPreferredChannel();
        }
        return this.preferredChannel;
    }

    private ChannelContract findPreferredChannel() {
        return new MatchingChannelSelector(this.matchingChannels)
                    .getPreferredChannel(this.targetIntervalLength)
                    .map(ChannelContract.class::cast)
                    .orElseThrow(() -> new IllegalStateException("Calculation of preferred channel failed before"));
    }

    void appendReferenceTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(this.sqlName());
        sqlBuilder.append(".");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.VALUE.sqlName());
    }

}