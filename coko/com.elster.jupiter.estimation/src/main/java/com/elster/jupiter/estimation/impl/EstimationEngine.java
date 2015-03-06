package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
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
        return decorate(findSuspects(channel).stream())
                .sorted(Comparator.comparing(ReadingQualityRecord::getReadingTimestamp))
                .map(this::toEstimatable)
                .partitionWhen((est1, est2) -> !channel.getNextDateTime(est1.getTimestamp()).equals(est2.getTimestamp()))
                .map(list -> SimpleEstimationBlock.of(channel, readingType, list));
    }

    private List<ReadingQualityRecord> findSuspects(Channel channel) {
        return channel.findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), Range.<Instant>all());
    }

    private Estimatable toEstimatable(ReadingQualityRecord readingQualityRecord) {
        return readingQualityRecord.getBaseReadingRecord()
                .map((baseReadingRecord) -> (Estimatable) new BaseReadingRecordEstimatable(baseReadingRecord))
                .orElseGet(() -> new MissingReadingRecordEstimatable(readingQualityRecord.getReadingTimestamp()));
    }
}
