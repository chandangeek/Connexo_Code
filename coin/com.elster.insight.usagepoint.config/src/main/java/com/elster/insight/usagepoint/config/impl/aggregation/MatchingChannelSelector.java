package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;

import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the algorithm that selects
 * the most appropriate Channel from a list of Channels
 * that match a {@link ReadingTypeRequirement}.<br>
 * Todo:
 * <ul>
 * <li>Take unit into account</li>
 * <li>Take unit's multiplier into account</li>
 * <li>Take accumulation (BULK, DELTA) into account</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (17:39)
 */
class MatchingChannelSelector {

    private final List<Channel> matchingChannels;

    MatchingChannelSelector(ReadingTypeRequirement requirement, MeterActivation meterActivation) {
        this(requirement.getMatchingChannelsFor(meterActivation));
    }

    MatchingChannelSelector(List<Channel> matchingChannels) {
        super();
        this.matchingChannels = Collections.unmodifiableList(matchingChannels);
    }

    /**
     * Returns the {@link IntervalLength} that is used by preference to extract data
     * for the {@link ReadingTypeRequirement} from the {@link MeterActivation}
     * that needs to be converted to the specified target IntervalLength.
     *
     * @return The preferred IntervalLength
     */
    Optional<IntervalLength> getPreferredInterval(IntervalLength targetInterval) {
        return this.getPreferredChannel(targetInterval).map(Channel::getMainReadingType).map(IntervalLength::from);
    }

    /**
     * Returns the {@link Channel} that is used by preference to extract data
     * for the {@link ReadingTypeRequirement} from the {@link MeterActivation}
     * that needs to be converted to the specified target IntervalLength.
     *
     * @return The preferred IntervalLength
     */
    Optional<Channel> getPreferredChannel(IntervalLength targetInterval) {
        /* For now, the preferred interval is the smallest matching reading type
         * that is compatible with the target interval. */
        return this.matchingChannels
                .stream()
                .sorted(new ChannelComparator(targetInterval))
                .filter(each -> this.areCompatible(each, targetInterval))
                .findFirst();
    }

    /**
     * Tests if the specified {@link IntervalLength} is supported to extract data
     * for the {@link ReadingTypeRequirement} from the {@link MeterActivation}.
     *
     * @param interval The IntervalLength
     * @return A flag that indicates if the IntervalLength is supported
     */
    boolean isIntervalSupported(IntervalLength interval) {
        return this.matchingChannels
                .stream()
                .map(Channel::getMainReadingType)
                .map(IntervalLength::from)
                .anyMatch(readingTypeInterval -> this.areCompatible(readingTypeInterval, interval));
    }

    private boolean areCompatible(IntervalLength first, IntervalLength second) {
        /* Todo: uncomment when adding support for disaggregation
         * return first.isMultipleOf(second) || first.multipliesTo(second);
         */
        return first.multipliesTo(second);
    }

    private boolean areCompatible(Channel channel, IntervalLength intervalLength) {
        return this.areCompatible(this.intervalLengthOf(channel), intervalLength);
    }

    private IntervalLength intervalLengthOf(Channel channel) {
        return IntervalLength.from(channel.getMainReadingType());
    }

    /**
     * Compares {@link Channel}s on {@link IntervalLength}
     * but prefers the ones that match the target IntervalLength,
     * i.e. it will make sure those are sorted first.
     */
    private class ChannelComparator implements Comparator<Channel> {
        private final IntervalLength targetInterval;
        private final Comparator<IntervalLength> delegate = new IntervalLengthComparator();

        private ChannelComparator(IntervalLength targetInterval) {
            super();
            this.targetInterval = targetInterval;
        }

        @Override
        public int compare(Channel o1, Channel o2) {
            IntervalLength il1 = intervalLengthOf(o1);
            IntervalLength il2 = intervalLengthOf(o2);
            if (il1 == this.targetInterval) {
                if (il2 == this.targetInterval) {
                    // Both match the target interval, consider them equal for now
                    return 0;
                }
                else {
                    // First Channel matches the target interval, sort it to the front
                    return -1;
                }
            }
            else {
                if (il2 == this.targetInterval) {
                    // Second Channel matches the target interval, sort it to the front
                    return 1;
                }
                else {
                    // None of the channels match the target interval, sort them by intervalLength
                    return this.delegate.compare(il1, il2);
                }
            }
        }
    }

}