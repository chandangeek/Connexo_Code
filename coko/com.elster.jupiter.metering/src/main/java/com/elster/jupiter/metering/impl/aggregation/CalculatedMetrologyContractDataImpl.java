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
import com.elster.jupiter.util.Ranges;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CalculatedMetrologyContractData} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-04 (10:24)
 */
class CalculatedMetrologyContractDataImpl implements CalculatedMetrologyContractData {

    private final InstantTruncaterFactory truncaterFactory;
    private final SourceChannelSetFactory sourceChannelSetFactory;
    private final UsagePoint usagePoint;
    private final MetrologyContract contract;
    private final Range<Instant> period;
    private final Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords;

    CalculatedMetrologyContractDataImpl(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period, Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords, InstantTruncaterFactory truncaterFactory, SourceChannelSetFactory sourceChannelSetFactory) {
        this.usagePoint = usagePoint;
        this.contract = contract;
        this.period = period;
        this.truncaterFactory = truncaterFactory;
        this.sourceChannelSetFactory = sourceChannelSetFactory;
        this.injectUsagePoint(calculatedReadingRecords);
        this.calculatedReadingRecords = this.generateTimeSeriesIfNecessary(contract, this.mergeMeterActivations(calculatedReadingRecords));
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
        calculatedReadingRecords
                .entrySet()
                .forEach(readingTypeAndRecords -> this.mergeMeterActivations(readingTypeAndRecords, merged));
        return merged;
    }

    private void mergeMeterActivations(Map.Entry<ReadingType, List<CalculatedReadingRecord>> readingTypeAndRecords, Map<ReadingType, List<CalculatedReadingRecord>> merged) {
        merged.put(readingTypeAndRecords.getKey(), this.merge(readingTypeAndRecords.getKey(), readingTypeAndRecords.getValue()));
    }

    private List<CalculatedReadingRecord> merge(ReadingType readingType, List<CalculatedReadingRecord> readingRecords) {
        Map<Instant, CalculatedReadingRecord> merged = new TreeMap<>(); // Keeps the keys sorted
        IntervalLength intervalLength = IntervalLength.from(readingType);
        readingRecords.forEach(record -> this.merge(record, this.truncaterFactory.truncaterFor(readingType), intervalLength, merged));
        return new ArrayList<>(merged.values());
    }

    private void merge(CalculatedReadingRecord record, InstantTruncater truncater, IntervalLength intervalLength, Map<Instant, CalculatedReadingRecord> merged) {
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
                (timestamp, existingRecord) -> existingRecord == null ? record : CalculatedReadingRecord.merge(existingRecord, record, endOfInterval, this.truncaterFactory, this.sourceChannelSetFactory));
    }

    private Map<ReadingType, List<CalculatedReadingRecord>> generateTimeSeriesIfNecessary(MetrologyContract contract, Map<ReadingType, List<CalculatedReadingRecord>> calculatedReadingRecords) {
        return calculatedReadingRecords
                .keySet()
                .stream()
                .collect(Collectors.toMap(
                        java.util.function.Function.identity(),
                        readingType -> this.generateTimeSeriesIfNecessary(
                                contract,
                                readingType,
                                calculatedReadingRecords.get(readingType))));
    }

    private List<CalculatedReadingRecord> generateTimeSeriesIfNecessary(MetrologyContract contract, ReadingType readingType, List<CalculatedReadingRecord> calculatedReadingRecords) {
        ExpressionNode expressionNode =
                contract
                    .getDeliverables()
                    .stream()
                    .filter(deliverable -> deliverable.getReadingType().getMRID().equals(readingType.getMRID()))
                    .map(ReadingTypeDeliverable::getFormula)
                    .map(Formula::getExpressionNode)
                    .findFirst()    // Guaranteed to find one: reading type is result from sql that was generated from the list of deliverables
                    .get();
        if (this.containsOnlyConstants(expressionNode)) {
            if (calculatedReadingRecords.size() == 1) {
                CalculatedReadingRecord record = calculatedReadingRecords.get(0);
                return IntervalLength
                            .from(readingType)
                            .toTimeSeries(this.period, this.usagePoint.getZoneId())
                            .map(record::atTimeStamp)
                            .collect(Collectors.toList());
            } else {
                return calculatedReadingRecords;
            }
        } else if (this.containsOnlyCustomProperties(expressionNode)) {
            // Assuming no gaps in custom property values
            RangeMap<Instant, CalculatedReadingRecord> recordRangeMap = this.toRangeMap(calculatedReadingRecords);
            return IntervalLength
                        .from(readingType)
                        .toTimeSeries(this.period, this.usagePoint.getZoneId())
                        .map(timestamp -> recordAtTimeStampOrNull(recordRangeMap, timestamp))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        } else {
            return calculatedReadingRecords;
        }
    }

    private CalculatedReadingRecord recordAtTimeStampOrNull(RangeMap<Instant, CalculatedReadingRecord> recordRangeMap, Instant timestamp) {
        CalculatedReadingRecord record = recordRangeMap.get(timestamp);
        if (record != null) {
            return record.atTimeStamp(timestamp);
        } else {
            return null;
        }
    }

    private boolean containsOnlyConstants(ExpressionNode expressionNode) {
        return expressionNode.accept(new ContainsOnlyConstants());
    }

    private boolean containsOnlyCustomProperties(ExpressionNode expressionNode) {
        return expressionNode.accept(new ContainsOnlyCustomProperties());
    }

    private RangeMap<Instant, CalculatedReadingRecord> toRangeMap(List<CalculatedReadingRecord> records) {
        RangeMap<Instant, CalculatedReadingRecord> map = TreeRangeMap.create();
        RangesBuilder rangesBuilder = new RangesBuilder(this.period.upperEndpoint());
        records.stream().sorted().forEach(record -> rangesBuilder.add(record.getTimeStamp()));
        rangesBuilder
                .allRanges()
                .forEach(range -> map.put(range, this.recordForRange(range, records)));
        return map;
    }

    private CalculatedReadingRecord recordForRange(Range<Instant> range, List<CalculatedReadingRecord> records) {
        return records
                    .stream()
                    .filter(record -> range.contains(record.getTimeStamp()))
                    .findFirst()
                    .get();
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

    private static class RangesBuilder {
        private final Instant lastInterval;
        private final RangeBuilder singleBuilder = new RangeBuilder();
        private final List<Range<Instant>> built = new ArrayList<>();

        private RangesBuilder(Instant lastInterval) {
            this.lastInterval = lastInterval;
        }

        public void add(Instant when) {
            if (this.lastInterval.isAfter(when)) {
                this.singleBuilder.add(when);
                if (this.singleBuilder.hasRange()) {
                    this.built.add(this.singleBuilder.getRange());
                    this.singleBuilder.reset();
                    this.singleBuilder.add(when);
                }
            }
        }

        List<Range<Instant>> allRanges() {
            this.singleBuilder.add(this.lastInterval);
            this.built.add(Ranges.copy(this.singleBuilder.getRange()).asClosed());
            List<Range<Instant>> ranges = new ArrayList<>(this.built);
            this.built.clear();
            this.singleBuilder.reset();
            return ranges;
        }
    }

    private static class RangeBuilder {
        private Instant start;
        private Instant end;

        void reset() {
            this.start = null;
            this.end = null;
        }

        void add(Instant when) {
            if (this.start == null) {
                this.start = when;
            } else if (this.end == null) {
                this.end = when;
            } else {
                throw new IllegalStateException("Current range is already complete");
            }
        }

        boolean hasRange() {
            return this.start != null && this.end != null;
        }

        Range<Instant> getRange() {
            if (this.start == null || this.end == null) {
                throw new IllegalStateException("Need to add at least 2 ranges before getting the range");
            }
            return Range.closedOpen(this.start, this.end);
        }

    }

}