package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class EstimationEngine {

    List<EstimationBlock> findBlocksToEstimate(MeterActivation meterActivation, ReadingType readingType) {
        return meterActivation.getChannels().stream()
                .filter(Channel::isRegular)
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .flatMap(channel -> this.findBlocksToEstimate(channel, readingType))
                .collect(Collectors.toList());
    }

    private Stream<EstimationBlock> findBlocksToEstimate(Channel channel, ReadingType readingType) {
        return decorate(findSuspects(channel, readingType).stream())
                .sorted(Comparator.comparing(ReadingQualityRecord::getReadingTimestamp))
                .map(this::toEstimatable)
                .partitionWhen((est1, est2) -> !channel.getNextDateTime(est1.getTimestamp()).equals(est2.getTimestamp()))
                .map(list -> SimpleEstimationBlock.of(channel, readingType, list));
    }

    private List<ReadingQualityRecord> findSuspects(Channel channel, ReadingType readingType) {
        return channel.getCimChannel(readingType)
                .map(cimChannel -> cimChannel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), Range.<Instant>all()))
                .orElse(Collections.emptyList());
    }

    private Estimatable toEstimatable(ReadingQualityRecord readingQualityRecord) {
        return readingQualityRecord.getBaseReadingRecord()
                .map((baseReadingRecord) -> (Estimatable) new BaseReadingRecordEstimatable(baseReadingRecord))
                .orElseGet(() -> new MissingReadingRecordEstimatable(readingQualityRecord.getReadingTimestamp()));
    }

    void applyEstimations(EstimationReportImpl report) {
        Map<CimChannelKey, List<EstimationBlock>> map = report.getResults().values().stream()
                .flatMap(estimationResult -> estimationResult.estimated().stream())
                .collect(Collectors.groupingBy(CimChannelKey::of));
        map.forEach((cimChannelKey, estimationBlocks) -> {
            List<BaseReading> readings = estimationBlocks.stream()
                    .map(ReadingEstimationBlock::new)
                    .flatMap(ReadingEstimationBlock::getReadings)
                    .collect(Collectors.toList());
            cimChannelKey.getCimChannel().estimateReadings(readings);
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
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
            return estimationBlock.estimatables().stream()
                    .map(estimatable -> ReadingImpl.of(null, estimatable.getEstimation(), estimatable.getTimestamp()))
                    .peek(reading -> reading.addQuality(estimationBlock.getReadingQualityType().getCode()));
        }
    }

}
