/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class EstimationEngine {

    List<EstimationBlock> findBlocksOfSuspectsToEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, ReadingType readingType) {
        return findBlocksToEstimate(system, channelsContainer, period, readingType, ImmutableSet.of(QualityCodeIndex.SUSPECT));
    }

    List<EstimationBlock> findBlocksToEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, ReadingType readingType) {
        return findBlocksToEstimate(system, channelsContainer, period, readingType, Arrays.stream(QualityCodeIndex.values())
                .collect(Collectors.toSet()));
    }

    private List<EstimationBlock> findBlocksToEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, Range<Instant> period, ReadingType readingType, Set<QualityCodeIndex> qualityCodes) {
        return channelsContainer.getChannels().stream()
                .filter(Channel::isRegular)
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .flatMap(channel -> findBlocksToEstimate(system, channel, period, readingType, qualityCodes))
                .collect(Collectors.toList());
    }

    private static Stream<EstimationBlock> findBlocksToEstimate(QualityCodeSystem system, Channel channel, Range<Instant> period, ReadingType readingType, Set<QualityCodeIndex> qualityCodes) {
        if (qualityCodes.size() > 1) {
            Map<Instant, BaseReadingRecord> readings = channel.getReadings(period).stream()
                    .collect(Collectors.toMap(BaseReadingRecord::getTimeStamp, Function.identity()));
            List<Estimatable> estimableList = channel.toList(period).stream()
                    .map(instant -> Optional.ofNullable(readings.get(instant))
                            .map(BaseReadingRecordEstimatable::new)
                            .map(Estimatable.class::cast)
                            .orElseGet(() -> new MissingReadingRecordEstimatable(instant)))
                    .collect(Collectors.toList());
            return estimableList.isEmpty() ? Stream.empty() : Stream.of(SimpleEstimationBlock.of(channel, readingType, estimableList));
        }

        return findBlocksBasedOnReadingQualities(system, channel, period, readingType, qualityCodes);
    }

    private static Stream<EstimationBlock> findBlocksBasedOnReadingQualities(QualityCodeSystem system, Channel channel, Range<Instant> period, ReadingType readingType, Set<QualityCodeIndex> qualityCodes) {
        return decorate(findReadingQualities(Collections.singleton(system), channel, period, readingType, qualityCodes).stream())
                .sorted(Comparator.comparing(ReadingQualityRecord::getReadingTimestamp))
                .map(EstimationEngine::toEstimatable)
                .partitionWhen((est1, est2) -> !channel.getNextDateTime(est1.getTimestamp())
                        .equals(est2.getTimestamp()))
                .filter(estimableList -> !estimableList.isEmpty())
                .map(estimableList -> SimpleEstimationBlock.of(channel, readingType, estimableList));
    }

    private static List<ReadingQualityRecord> findReadingQualities(Set<QualityCodeSystem> systems, Channel channel, Range<Instant> period, ReadingType readingType, Set<QualityCodeIndex> qualityCodes) {
        return channel.getCimChannel(readingType)
                .map(cimChannel -> cimChannel.findReadingQualities()
                        .ofQualitySystems(systems)
                        .ofQualityIndices(qualityCodes)
                        .inTimeInterval(period)
                        .collect())
                .orElse(Collections.emptyList());
    }

    private static Estimatable toEstimatable(ReadingQualityRecord readingQualityRecord) {
        return readingQualityRecord.getBaseReadingRecord()
                .map(BaseReadingRecordEstimatable::new)
                .map(Estimatable.class::cast)
                .orElse(new MissingReadingRecordEstimatable(readingQualityRecord.getReadingTimestamp()));
    }

    void applyEstimations(QualityCodeSystem system, EstimationReportImpl report) {
        Map<CimChannelKey, List<EstimationBlock>> map = report.getResults().values().stream()
                .flatMap(estimationResult -> estimationResult.estimated().stream())
                .collect(Collectors.groupingBy(CimChannelKey::of));
        map.forEach((cimChannelKey, estimationBlocks) -> {
            List<BaseReading> readings = estimationBlocks.stream()
                    .map(ReadingEstimationBlock::new)
                    .flatMap(ReadingEstimationBlock::getReadings)
                    .collect(Collectors.toList());
            cimChannelKey.getCimChannel().estimateReadings(system, readings);
        });
    }

    private static final class CimChannelKey {
        private final CimChannel cimChannel;

        private CimChannelKey(CimChannel cimChannel) {
            this.cimChannel = cimChannel;
        }

        public CimChannel getCimChannel() {
            return cimChannel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CimChannelKey that = (CimChannelKey) o;
            return cimChannel.getChannel().getId() == that.cimChannel.getChannel().getId()
                    && Objects.equals(cimChannel.getReadingType(), that.cimChannel.getReadingType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(cimChannel.getChannel().getId(), cimChannel.getReadingType());
        }

        public static CimChannelKey of(EstimationBlock estimationBlock) {
            return new CimChannelKey(estimationBlock.getChannel().getCimChannel(estimationBlock.getReadingType()).orElseThrow(IllegalArgumentException::new));
        }
    }

    private static class ReadingEstimationBlock {
        private final EstimationBlock estimationBlock;

        public ReadingEstimationBlock(EstimationBlock estimationBlock) {
            this.estimationBlock = estimationBlock;
        }

        private Stream<? extends BaseReading> getReadings() {
            Map<ReadingQualityType, ReadingQualityComment> readingQualityTypeWithComments = estimationBlock.getReadingQualityTypesWithComments();
            Set<ReadingQualityType> readingQualityTypes = readingQualityTypeWithComments.keySet();
            return estimationBlock.estimatables().stream()
                    .map(estimatable -> ReadingImpl.of(estimationBlock.getReadingType().getMRID(), estimatable.getEstimation(), estimatable.getTimestamp()))
                    .peek(reading -> readingQualityTypes.forEach(quality -> {
                        ReadingQualityComment readingQualityComment = readingQualityTypeWithComments.get(quality);
                        reading.addQuality(quality.getCode(), readingQualityComment != null ? readingQualityComment.getComment() : null);
                    }));
        }
    }

}
