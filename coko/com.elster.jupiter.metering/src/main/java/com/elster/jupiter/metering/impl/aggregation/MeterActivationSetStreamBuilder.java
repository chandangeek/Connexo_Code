package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * Builds a Stream of {@link MeterActivationSet}
 * for a {@link UsagePoint} and a data aggregation period.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-09 (21:22)
 */
class MeterActivationSetStreamBuilder {

    private final UsagePoint usagePoint;
    private final Range<Instant> period;
    private MeterActivationSetImpl lastBuilt;

    MeterActivationSetStreamBuilder(UsagePoint usagePoint, Range<Instant> period) {
        this.usagePoint = usagePoint;
        this.period = period;
    }

    MeterActivationSetStreamBuilder(UsagePoint usagePoint, Instant when) {
        this(usagePoint, Range.singleton(when));
    }

    Stream<MeterActivationSet> build() {
        return this.getOverlappingMeterActivations()
                .flatMap(this::switchTimestamps)
                .distinct()
                .sorted()
                .map(this::createMeterActivationSet);
    }

    private Stream<Instant> switchTimestamps(MeterActivation meterActivation) {
        return this.switchTimestampsFromMeterActivationRange(meterActivation.getRange());
    }

    private Stream<Instant> switchTimestampsFromMeterActivationRange(Range<Instant> meterActivationRange) {
        Stream.Builder<Instant> builder = Stream.builder();
        builder.add(meterActivationRange.lowerEndpoint());
        if (meterActivationRange.hasUpperBound()) {
            builder.add(meterActivationRange.upperEndpoint());
        }
        return builder.build();
    }

    private Stream<MeterActivation> getOverlappingMeterActivations() {
        return this.usagePoint.getMeterActivations().stream().filter(each -> each.overlaps(this.period));
    }

    private MeterActivationSet createMeterActivationSet(Instant startDate) {
        return this.createMeterActivationSet(
                startDate,
                this.getOverlappingMeterActivations()
                        .filter(meterActivation -> meterActivation.getRange().contains(startDate)));
    }

    private MeterActivationSet createMeterActivationSet(Instant startDate, Stream<MeterActivation> meterActivations) {
        int sequenceNumber;
        if (this.lastBuilt != null) {
            this.lastBuilt.endAt(startDate);
            sequenceNumber = this.lastBuilt.sequenceNumber() + 1;
        } else {
            sequenceNumber = 1;
        }
        MeterActivationSetImpl set = new MeterActivationSetImpl(this.usagePoint.getEffectiveMetrologyConfiguration(this.period.lowerEndpoint())
                .get()
                .getMetrologyConfiguration(), sequenceNumber, startDate);
        meterActivations.forEach(set::add);
        this.lastBuilt = set;
        return set;
    }

}