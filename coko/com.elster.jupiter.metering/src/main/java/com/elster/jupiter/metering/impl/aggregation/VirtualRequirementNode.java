/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.ChannelContract;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ExpressionNode} interface
 * for a reference to a {@link VirtualReadingTypeRequirement}
 * in an expression tree that defines the calculation for
 * a {@link ReadingTypeDeliverable}.
 * <p>
 * It will participate in generating a "with" clause that will
 * do time based aggregation if the interval of the target reading
 * type &gt; interval of the most appropriate channel.
 * It will therefore also need to participate in the select clause
 * that relates to the {@link ReadingTypeDeliverable} because
 * the available data (column names and types) is different
 * for raw timeseries data and aggregated data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:28)
 */
class VirtualRequirementNode implements ServerExpressionNode {

    private final Formula.Mode mode;
    private final VirtualFactory virtualFactory;
    private final ReadingTypeRequirement requirement;
    private final ReadingTypeDeliverable deliverable;
    private final MeterActivationSet meterActivationSet;
    private VirtualReadingType targetReadingType;
    private VirtualReadingTypeRequirement virtualRequirement;
    private boolean finished;

    VirtualRequirementNode(Formula.Mode mode, VirtualFactory virtualFactory, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, MeterActivationSet meterActivationSet) {
        super();
        this.mode = mode;
        this.virtualFactory = virtualFactory;
        this.requirement = requirement;
        this.deliverable = deliverable;
        this.meterActivationSet = meterActivationSet;
        this.targetReadingType = this.preferredReadingTypeFor(VirtualReadingType.from(this.deliverable.getReadingType()));
        if (this.targetReadingType.isUnsupported()) {
            this.targetReadingType = VirtualReadingType.from(this.deliverable.getReadingType());
        }
        this.finished = false;
    }

    ReadingTypeRequirement getRequirement() {
        return requirement;
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(this.getSourceReadingType().getDimension());
    }

    /**
     * Calculates the preferred target reading type,
     * taking all the backing channels of the actual
     * {@link ReadingTypeRequirement} into account.
     *
     * @return The preferred reading type
     */
    VirtualReadingType getPreferredReadingType() {
        return this.preferredReadingTypeFor(this.getTargetReadingType());
    }

    private VirtualReadingType preferredReadingTypeFor(VirtualReadingType targetReadingType) {
        /* Preferred interval is the smallest matching reading type
         * that is compatible with the target interval. */
        Optional<VirtualReadingType> preferredReadingType = new MatchingChannelSelector(this.requirement, this.meterActivationSet, this.mode)
                .getPreferredReadingType(targetReadingType);
        if (preferredReadingType.isPresent()) {
            Loggers.ANALYSIS.debug(() ->
                    MessageFormat.format(
                            "Preferred reading type for requirement ''{0}'' in meter activation {1} for the calculation of deliverable ''{2}'' : {3}",
                            this.requirement.getName() + "-" + this.requirementReadingTypeForLogging(),
                            this.meterActivationSet.getRange(),
                            this.deliverable.getName() + "-" + targetReadingType,
                            preferredReadingType.get().toString()));
            return preferredReadingType.get();
        } else {
            Loggers.ANALYSIS.severe(() ->
                    MessageFormat.format(
                            "Unable to find matching channel for the requirement ''{0}'' in meter activation {1} as part of calculation for deliverable ''{2}''",
                            this.requirement.getName() + "-" + this.requirementReadingTypeForLogging(),
                            this.meterActivationSet.getRange().toString(),
                            this.deliverable.getName() + "-" + targetReadingType));
            Loggers.ANALYSIS.debug(() -> verboseAvailableMainReadingTypesOnMeterActivation(this.meterActivationSet));
            return VirtualReadingType.notSupported();
        }
    }

    private String requirementReadingTypeForLogging() {
        if (this.requirement instanceof FullySpecifiedReadingTypeRequirement) {
            FullySpecifiedReadingTypeRequirement requirement = (FullySpecifiedReadingTypeRequirement) this.requirement;
            return requirement.getReadingType().getMRID();
        } else {
            PartiallySpecifiedReadingTypeRequirement requirement = (PartiallySpecifiedReadingTypeRequirement) this.requirement;
            return requirement.getReadingTypeTemplate().toString();
        }
    }

    private String verboseAvailableMainReadingTypesOnMeterActivation(MeterActivationSet meterActivationSet) {
        List<Channel> channels = meterActivationSet.getChannels();
        if (!channels.isEmpty()) {
            return "The following (main) reading types are available:\n\t"
                    + channels
                    .stream()
                    .map(Channel::getMainReadingType)
                    .map(ReadingType::getMRID)
                    .collect(Collectors.joining(",\n\t"));
        } else {
            return "No channels are available in the meter activation";
        }
    }

    /**
     * Tests if the specified {@link VirtualReadingType}
     * can be supported by looking at the backing channels of
     * the actual {@link ReadingTypeRequirement}.
     *
     * @param readingType The readingType
     * @return A flag that indicates if the readingType is backed by one of the channels
     */
    boolean supports(VirtualReadingType readingType) {
        return new MatchingChannelSelector(this.requirement, this.meterActivationSet, this.mode).isReadingTypeSupported(readingType);
    }

    /**
     * Tests if the specified {@link VirtualReadingType}
     * can be supported in a {@link UnitConversionNode}
     * by looking at the backing channels of
     * the actual {@link ReadingTypeRequirement}.
     *
     * @param readingType The readingType
     * @return A flag that indicates if the readingType is backed by one of the channels
     */
    boolean supportsInUnitConversion(VirtualReadingType readingType) {
        return new MatchingChannelSelector(this.requirement, this.meterActivationSet, this.mode).isReadingTypeSupportedInUnitConversion(readingType);
    }

    VirtualReadingType getSourceReadingType() {
        if (this.finished) {
            return this.virtualRequirement.getSourceReadingType();
        } else {
            return this.getTargetReadingType();
        }
    }

    ReadingType getDeliverableReadingType() {
        return this.deliverable.getReadingType();
    }

    ChannelContract getPreferredChannel() {
        this.ensureVirtualized();
        return this.virtualRequirement.getPreferredChannel();
    }

    VirtualReadingType getTargetReadingType() {
        return this.targetReadingType;
    }

    void setTargetReadingType(VirtualReadingType targetReadingType) {
        this.targetReadingType = targetReadingType;
        if (this.virtualRequirement != null) {
            this.virtualRequirement.setTargetReadingType(targetReadingType);
        }
    }

    void setTargetIntervalLength(IntervalLength intervalLength) {
        this.setTargetReadingType(this.targetReadingType.withIntervalLength(intervalLength));
    }

    void setTargetMultiplier(MetricMultiplier multiplier) {
        this.setTargetReadingType(this.targetReadingType.withMetricMultiplier(multiplier));
    }

    void setTargetCommodity(Commodity commodity) {
        this.setTargetReadingType(this.targetReadingType.withCommondity(commodity));
    }

    void finish() {
        this.ensureVirtualized();
        this.finished = true;
    }

    /**
     * Ensures that the {@link ReadingTypeRequirement} is virtualized
     *
     * @see #virtualize()
     */
    private void ensureVirtualized() {
        if (this.virtualRequirement == null) {
            this.virtualize();
        }
    }

    /**
     * Creates the {@link VirtualReadingTypeRequirement} from the current target interval.
     */
    private void virtualize() {
        this.virtualRequirement = this.virtualFactory.requirementFor(this.mode, this.requirement, this.deliverable, this.targetReadingType);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVirtualRequirement(this);
    }

    /**
     * Appends the necessary sql constructs to the specified {@link SqlBuilder}
     * to get the simple value of this nodes's {@link ReadingTypeRequirement}.
     *
     * @param sqlBuilder The SqlBuilder
     */
    void appendTo(SqlBuilder sqlBuilder) {
        this.ensureVirtualized();
        this.virtualRequirement.appendSimpleReferenceTo(sqlBuilder);
    }

    /**
     * Appends the necessary sql constructs to the specified {@link SqlBuilder}
     * to get the value of this nodes's {@link ReadingTypeRequirement}
     * and apply unit conversion if that is necessary.
     *
     * @param sqlBuilder The SqlBuilder
     */
    void appendToWithUnitConversion(SqlBuilder sqlBuilder) {
        this.ensureVirtualized();
        this.virtualRequirement.appendReferenceTo(sqlBuilder);
    }

    String sqlName() {
        this.ensureVirtualized();
        return this.virtualRequirement.sqlName();
    }

}