package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CalculatedMetrologyContractData} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-04 (10:24)
 */
class CalculatedMetrologyContractDataImpl implements CalculatedMetrologyContractData {

    private final ServerMeteringService meteringService;
    private final UsagePoint usagePoint;
    private final MetrologyContract contract;
    private final Range<Instant> period;
    private final Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords;

    CalculatedMetrologyContractDataImpl(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period, Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords, ServerMeteringService meteringService) {
        this.usagePoint = usagePoint;
        this.contract = contract;
        this.period = period;
        this.meteringService = meteringService;
        this.injectUsagePoint(calculatedReadingRecords);
        this.calculatedReadingRecords = this.generateConstantsAsTimeSeries(contract, this.mergeMeterActivations(calculatedReadingRecords));
    }

    private void injectUsagePoint(Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords) {
        calculatedReadingRecords
                .values()
                .stream()
                .flatMap(List::stream)
                .forEach(this::injectUsagePoint);
    }

    private void injectUsagePoint(CalculatedReadingRecord calculatedReadingRecord) {
        calculatedReadingRecord.setUsagePoint(this.usagePoint);
    }

    /**
     * Merges multiple {@link CalculatedReadingRecord}s that relate to different
     * {@link com.elster.jupiter.metering.MeterActivation}s within the same
     * aggregation period. As an example, when a different meter was activated
     * on a usage point in the middle of a month and aggregation is requested
     * for one month then the two meter activations result in two different
     * CalculatedReadingRecords because of the way the SQL is structured.
     * These two CalculatedReadingRecords need to be merged to one and the same.
     *
     * @param calculatedReadingRecords The List of CalculatedReadingRecord organized by {@link ReadingType}
     * @return The List of CalculatedReadingRecord organized by ReadingType spanning only periods that relate to the aggregation specified in the ReadingType
     */
    private Map<ReadingType, List<CalculatedReadingRecord>> mergeMeterActivations(Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords) {
        Map<ReadingType, List<CalculatedReadingRecord>> merged = new HashMap<>();
        calculatedReadingRecords.entrySet().forEach(readingTypeAndRecords -> this.mergeMeterActivations(readingTypeAndRecords, merged));
        return merged;
    }

    private void mergeMeterActivations(Map.Entry<ReadingType, List<CalculatedReadingRecord>> readingTypeAndRecords, Map<ReadingType, List<CalculatedReadingRecord>> merged) {
        merged.put(readingTypeAndRecords.getKey(), this.merge(readingTypeAndRecords.getKey(), readingTypeAndRecords.getValue()));
    }

    private List<CalculatedReadingRecord> merge(ReadingType readingType, List<CalculatedReadingRecord> readingRecords) {
        Map<Instant, CalculatedReadingRecord> merged = new TreeMap<>(); // Keeps the keys sorted
        IntervalLength intervalLength = IntervalLength.from(readingType);
        readingRecords.forEach(record -> this.merge(record, this.truncaterFor(readingType), intervalLength, merged));
        return new ArrayList<>(merged.values());
    }

    private void merge(CalculatedReadingRecord record, Truncater truncater, IntervalLength intervalLength, Map<Instant, CalculatedReadingRecord> merged) {
        ZoneId zone = this.getUsagePoint()
                .getMeterActivation(record.getTimeStamp())
                .map(MeterActivation::getChannelsContainer)
                .map(ChannelsContainer::getZoneId)
                .orElseGet(this.usagePoint::getZoneId);
        final Instant endOfInterval;
        Instant endOfIntervalCandidate = truncater.truncate(record.getTimeStamp(), intervalLength, zone);
        if (!endOfIntervalCandidate.equals(record.getTimeStamp())) {
            // Timestamp was not aligned with interval
            endOfInterval = intervalLength.addTo(endOfIntervalCandidate, zone);
        } else {
            endOfInterval = endOfIntervalCandidate;
        }
        merged.compute(
                endOfInterval,
                (timestamp, existingRecord) -> existingRecord == null ? record : CalculatedReadingRecord.merge(existingRecord, record, endOfInterval));
    }

    /**
     * Generates the single record that was produced for a deliverable
     * whose formula only contains constants, as a timeseries that
     * produces that constant value at every expected interval.
     *
     * @param contract The MetrologyContract
     * @param calculatedReadingRecords The List of CalculatedReadingRecord
     * @return The Map that will now contain a List of CalculatedReadingRecord instead of a single CalculatedReadingRecord
     * for the ReadingTypes whose formula only contains constants
     */
    private Map<ReadingType, List<CalculatedReadingRecord>> generateConstantsAsTimeSeries(MetrologyContract contract, Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords) {
        return calculatedReadingRecords
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        java.util.function.Function.identity(),
                        readingType -> this.generateConstantsAsTimeSeries(
                                contract,
                                readingType,
                                calculatedReadingRecords.get(readingType))));
    }

    private List<CalculatedReadingRecord> generateConstantsAsTimeSeries(MetrologyContract contract, ReadingType readingType, List<CalculatedReadingRecord> calculatedReadingRecords) {
        if (calculatedReadingRecords.size() == 1) {
            ExpressionNode expressionNode =
                    contract
                            .getDeliverables()
                            .stream()
                            .filter(deliverable -> deliverable.getReadingType().getMRID().equals(readingType.getMRID()))
                            .map(ReadingTypeDeliverable::getFormula)
                            .map(Formula::getExpressionNode)
                            .findFirst()    // Guaranteed to find one: reading type is result from sql that was generated from the list of deliverables
                            .get();
            if (expressionNode.accept(new ContainsOnlyConstants())) {
                CalculatedReadingRecord record = calculatedReadingRecords.get(0);
                return IntervalLength
                        .from(readingType)
                        .toTimeSeries(this.period, this.usagePoint.getZoneId())
                        .map(record::atTimeStamp)
                        .collect(Collectors.toList());
            } else {
                return calculatedReadingRecords;
            }
        } else {
            return calculatedReadingRecords;
        }
    }

    @Override
    public UsagePoint getUsagePoint() {
        return this.usagePoint;
    }

    @Override
    public MetrologyContract getMetrologyContract() {
        return this.contract;
    }

    @Override
    public boolean isEmpty() {
        return this.calculatedReadingRecords.isEmpty();
    }

    @Override
    public List<? extends BaseReadingRecord> getCalculatedDataFor(ReadingTypeDeliverable deliverable) {
        return this.calculatedReadingRecords.getOrDefault(deliverable.getReadingType(), Collections.emptyList());
    }

    private Truncater truncaterFor(ReadingType readingType) {
        if (VirtualReadingType.isGas(readingType.getCommodity())) {
            GasDayOptions gasDayOptions = this.meteringService.getGasDayOptions();
            if (gasDayOptions != null) {
                return new GasDayTruncater(gasDayOptions);
            } else {
                return new MidnightTruncater();
            }
        } else {
            return new MidnightTruncater();
        }
    }

    private interface Truncater {
        Instant truncate(Instant instant, IntervalLength intervalLength, ZoneId zoneId);
    }

    private static class MidnightTruncater implements Truncater {
        @Override
        public Instant truncate(Instant instant, IntervalLength intervalLength, ZoneId zoneId) {
            return intervalLength.truncate(instant, zoneId);
        }
    }

    private static class GasDayTruncater implements Truncater {
        private final GasDayOptions gasDayOptions;

        private GasDayTruncater(GasDayOptions gasDayOptions) {
            this.gasDayOptions = gasDayOptions;
        }

        @Override
        public Instant truncate(Instant instant, IntervalLength intervalLength, ZoneId zoneId) {
            switch (intervalLength) {
                case YEAR1: // Intentional fall-through
                case DAY1: {
                    int hours = this.gasDayOptions.getYearStart().getHour();
                    if (hours > 0) {
                        return intervalLength.truncate(instant.minus(hours, ChronoUnit.HOURS), zoneId).plus(hours, ChronoUnit.HOURS);
                    } else {
                        return intervalLength.truncate(instant, zoneId);
                    }
                }
                default: {
                    return intervalLength.truncate(instant, zoneId);
                }
            }
        }
    }
}