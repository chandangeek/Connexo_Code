/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the algorithm that selects
 * the most appropriate Channel from a list of Channels
 * that match a {@link ReadingTypeRequirement}.<br>
 * Todo:
 * <ul>
 * <li>Take accumulation (BULK, DELTA) into account</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (17:39)
 */
class MatchingChannelSelector {

    private final List<Channel> matchingChannels;
    private final Formula.Mode mode;

    MatchingChannelSelector(ReadingTypeRequirement requirement, MeterActivationSet meterActivationSet, Formula.Mode mode) {
        this(meterActivationSet.getMatchingChannelsFor(requirement), mode);
    }

    MatchingChannelSelector(List<Channel> matchingChannels, Formula.Mode mode) {
        super();
        this.mode = mode;
        if (matchingChannels.isEmpty()) {
            Loggers.ANALYSIS.severe(() -> "No matching channels to start the search for preferred channel. Must be running against a meter that was activated on the usage point that does not fulfill the requirements of the usage point's metrology configuration.");
        }
        this.matchingChannels = Collections.unmodifiableList(matchingChannels);
    }

    /**
     * Returns the {@link VirtualReadingType} that is used by preference to extract data
     * for the {@link ReadingTypeRequirement} from the {@link MeterActivation}
     * that needs to be converted to the specified target VirtualReadingType.
     *
     * @return The preferred VirtualReadingType
     */
    Optional<VirtualReadingType> getPreferredReadingType(VirtualReadingType readingType) {
        return this.getPreferredChannel(readingType).map(Channel::getMainReadingType).map(VirtualReadingType::from);
    }

    /**
     * Returns the {@link Channel} that is used by preference to extract data
     * for the {@link ReadingTypeRequirement} from the {@link MeterActivation}
     * that needs to be converted to the specified target VirtualReadingType.
     *
     * @return The preferred VirtualReadingType
     */
    Optional<Channel> getPreferredChannel(VirtualReadingType readingType) {
        Stream<Channel> sortedChannels = this.matchingChannels.stream().sorted(new ChannelComparator(readingType));
        if (this.mode.equals(Formula.Mode.AUTO)) {
            /* For now, the preferred interval is the smallest matching reading type
             * that is compatible with the target interval. */
            return sortedChannels
                    .filter(each -> this.areCompatible(each, readingType))
                    .findFirst();
        } else {
            // Sort order is good enough for the expert
            return sortedChannels.findFirst();
        }
    }

    /**
     * Tests if the specified {@link VirtualReadingType} is supported to extract data
     * for the {@link ReadingTypeRequirement} from the {@link MeterActivation}.
     *
     * @param readingType The VirtualReadingType
     * @return A flag that indicates if the VirtualReadingType is supported
     */
    boolean isReadingTypeSupported(VirtualReadingType readingType) {
        return this.matchingChannels
                .stream()
                .map(Channel::getMainReadingType)
                .map(VirtualReadingType::from)
                .anyMatch(each -> this.areCompatible(each, readingType));
    }

    private boolean areCompatible(VirtualReadingType first, VirtualReadingType second) {
        if (first.isRegular() && second.isRegular()) {
            /* Both are regular so they are compatible if
             *      interval lengths are compatible
             *  and units are compatible for unit conversion. */
            return first.getIntervalLength().multipliesTo(second.getIntervalLength())
                    && UnitConversionSupport.areCompatibleForAutomaticUnitConversion(first.getUnit(), second.getUnit());
        } else if (!first.isRegular() && !second.isRegular()) {
            // Both are irregular so they are compatible if units are compatible for unit conversion
            return UnitConversionSupport.areCompatibleForAutomaticUnitConversion(first.getUnit(), second.getUnit());
        } else {
            // One is regular and the other is not so they are not compatible
            return false;
        }
    }

    /**
     * Tests if the specified {@link VirtualReadingType} is supported
     * in a {@link UnitConversionNode} to extract data
     * for the {@link ReadingTypeRequirement} from the {@link MeterActivation}.
     *
     * @param readingType The VirtualReadingType
     * @return A flag that indicates if the VirtualReadingType is supported
     */
    boolean isReadingTypeSupportedInUnitConversion(VirtualReadingType readingType) {
        return this.matchingChannels
                .stream()
                .map(Channel::getMainReadingType)
                .map(VirtualReadingType::from)
                .anyMatch(each -> this.areCompatibleInUnitConversion(each, readingType));
    }

    private boolean areCompatibleInUnitConversion(VirtualReadingType first, VirtualReadingType second) {
        return this.areCompatible(first, second)
                || (first.isFlowRelated() && second.isFlowRelated())
                || (first.isVolumeRelated() && second.isVolumeRelated());
    }

    private boolean areCompatible(Channel channel, VirtualReadingType intervalLength) {
        return this.areCompatible(this.readingTypeFor(channel), intervalLength);
    }

    private VirtualReadingType readingTypeFor(Channel channel) {
        return VirtualReadingType.from(channel.getMainReadingType());
    }

    /**
     * Compares {@link Channel}s on {@link IntervalLength}
     * but prefers the ones that match the target IntervalLength,
     * i.e. it will make sure those are sorted first.
     */
    private class ChannelComparator implements Comparator<Channel> {
        private final VirtualReadingType targetReadingType;

        private ChannelComparator(VirtualReadingType targetReadingType) {
            super();
            this.targetReadingType = targetReadingType;
        }

        @Override
        public int compare(Channel o1, Channel o2) {
            VirtualReadingType rt1 = readingTypeFor(o1);
            VirtualReadingType rt2 = readingTypeFor(o2);
            int il1ComparedToTarget = rt1.compareTo(this.targetReadingType);
            int il2ComparedToTarget = rt2.compareTo(this.targetReadingType);
            if (compareSameToTargetReadingType(il1ComparedToTarget, il2ComparedToTarget)) {
                /* Both compare the same way to the target reading type
                 * so they are both smaller, bigger or equal to the target reading type. */
                if (il1ComparedToTarget == 0) {
                    // Both are equal to the target reading type, consider them equal for now
                    return 0;
                } else if (il1ComparedToTarget < 0) {
                    // Both are smaller, sort them in descending order
                    return -rt1.compareTo(rt2);
                } else {
                    // Both are bigger, sort them in ascending order
                    return rt1.compareTo(rt2);
                }
            } else if (il1ComparedToTarget == 0) {
                // rt2 != target reading type
                return -1;
            } else if (il2ComparedToTarget == 0) {
                // rt1 != target reading type
                return 1;
            } else if (il1ComparedToTarget < 0) {
                // rt1 < target reading type < rt2
                return -1;
            } else {
                // rt2 < target readng type < rt1
                return 1;
            }
        }

        private boolean compareSameToTargetReadingType(int il1ComparedToTarget, int il2ComparedToTarget) {
            return (il1ComparedToTarget < 0 && il2ComparedToTarget < 0)
                    || (il1ComparedToTarget == 0 && il2ComparedToTarget == 0)
                    || (il1ComparedToTarget > 0 && il2ComparedToTarget > 0);
        }

    }

}