/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.BaseReadingImpl;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

class EditedChannelReadingSet {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final TopologyService topologyService;
    private final Channel channel;

    private final List<BaseReading> editedReadings = new ArrayList<>();
    private final List<BaseReading> editedBulkReadings = new ArrayList<>();
    private final List<BaseReading> confirmedReadings = new ArrayList<>();
    private final List<BaseReading> estimatedReadings = new ArrayList<>();
    private final List<BaseReading> estimatedBulkReadings = new ArrayList<>();
    private final List<Instant> removeCandidates = new ArrayList<>();

    private final Map<Long, Optional<ReadingQualityComment>> readingQualitiesComments = new HashMap<>();

    EditedChannelReadingSet(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, TopologyService topologyService, Channel channel) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.topologyService = topologyService;
        this.channel = channel;
    }

    List<BaseReading> getEditedReadings() {
        return Collections.unmodifiableList(editedReadings);
    }

    List<BaseReading> getEditedBulkReadings() {
        return Collections.unmodifiableList(editedBulkReadings);
    }

    List<BaseReading> getConfirmedReadings() {
        return Collections.unmodifiableList(confirmedReadings);
    }

    List<BaseReading> getEstimatedReadings() {
        return Collections.unmodifiableList(estimatedReadings);
    }

    List<BaseReading> getEstimatedBulkReadings() {
        return Collections.unmodifiableList(estimatedBulkReadings);
    }

    List<Instant> getRemoveCandidates() {
        return Collections.unmodifiableList(removeCandidates);
    }

    Optional<Instant> getFirstEditedReadingTime() {
        return Stream.of(
                removeCandidates.stream(),
                editedReadings.stream().map(BaseReading::getTimeStamp),
                editedBulkReadings.stream().map(BaseReading::getTimeStamp),
                estimatedReadings.stream().map(BaseReading::getTimeStamp),
                estimatedBulkReadings.stream().map(BaseReading::getTimeStamp),
                confirmedReadings.stream().map(BaseReading::getTimeStamp))
                .flatMap(Function.identity())
                .min(Comparator.naturalOrder());
    }

    EditedChannelReadingSet init(List<ChannelDataInfo> channelDataInfos) {
        validateLinkedToSlave(this.channel, channelDataInfos);
        channelDataInfos.forEach(this::processInfo);
        return this;
    }

    private void processInfo(ChannelDataInfo channelDataInfo) {
        if (!(isToBeConfirmed(channelDataInfo)) && channelDataInfo.value == null && channelDataInfo.collectedValue == null) {
            this.removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
        } else {
            processCalculatedInfo(channelDataInfo);
            processBulkInfo(channelDataInfo);
            processConfirmedInfo(channelDataInfo);
        }
    }

    private void validateLinkedToSlave(Channel channel, List<ChannelDataInfo> channelDataInfos) {
        Optional<Long> min = channelDataInfos.stream().map(channelDataInfo -> channelDataInfo.interval.start).min(Comparator.naturalOrder());
        Optional<Long> max = channelDataInfos.stream().map(channelDataInfo -> channelDataInfo.interval.end).max(Comparator.naturalOrder());
        if (min.isPresent() && max.isPresent()) {
            List<DataLoggerChannelUsage> dataLoggerChannelUsagesForChannels = topologyService.findDataLoggerChannelUsagesForChannels(channel, Range.closedOpen(Instant.ofEpochMilli(min.get()), Instant.ofEpochMilli(max
                    .get())));

            if (!dataLoggerChannelUsagesForChannels.isEmpty() && !channelDataInfos.stream().map(channelDataInfo -> channelDataInfo.interval)
                    .map(intervalInfo -> Range.closedOpen(Instant.ofEpochMilli(intervalInfo.start), Instant.ofEpochMilli(intervalInfo.end)))
                    .allMatch(intervalRange -> dataLoggerChannelUsagesForChannels.stream().anyMatch(channelUsage -> channelUsage.getRange().encloses(intervalRange)))) {
                throw this.exceptionFactory.newException(MessageSeeds.CANNOT_ADDEDITREMOVE_CHANNEL_VALUE_WHEN_LINKED_TO_SLAVE);
            }
        }
    }

    private void processCalculatedInfo(ChannelDataInfo channelDataInfo) {
        if (channelDataInfo.value != null) {
            BaseReadingImpl reading = channelDataInfo.createNew();
            String comment = extractComment(channelDataInfo.mainValidationInfo);
            if (channelDataInfo.mainValidationInfo != null && channelDataInfo.mainValidationInfo.ruleId != 0) {
                reading.addQuality(estimatedReadingQualityTypeCode(channelDataInfo.mainValidationInfo.ruleId), comment);
                this.estimatedReadings.add(reading);
            } else {
                reading.addQuality(editedReadingQualityTypeCode(), comment);
                this.editedReadings.add(reading);
            }
        }
    }

    private void processBulkInfo(ChannelDataInfo channelDataInfo) {
        if (channelDataInfo.collectedValue != null) {
            BaseReadingImpl reading = channelDataInfo.createNewBulk();
            String comment = extractComment(channelDataInfo.bulkValidationInfo);
            if (channelDataInfo.bulkValidationInfo != null && channelDataInfo.bulkValidationInfo.ruleId != 0) {
                reading.addQuality(estimatedReadingQualityTypeCode(channelDataInfo.bulkValidationInfo.ruleId), comment);
                this.estimatedBulkReadings.add(reading);
            } else {
                reading.addQuality(editedReadingQualityTypeCode(), comment);
                this.editedBulkReadings.add(reading);
            }
        }
    }

    private void processConfirmedInfo(ChannelDataInfo channelDataInfo) {
        if (isToBeConfirmed(channelDataInfo)) {
            this.confirmedReadings.add(channelDataInfo.createConfirm());
        }
    }

    private boolean isToBeConfirmed(ChannelDataInfo channelDataInfo) {
        return channelDataInfo.mainValidationInfo != null && channelDataInfo.mainValidationInfo.isConfirmed ||
                channelDataInfo.bulkValidationInfo != null && channelDataInfo.bulkValidationInfo.isConfirmed;
    }

    private String editedReadingQualityTypeCode() {
        return ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC).getCode();
    }

    private String estimatedReadingQualityTypeCode(long ruleId) {
        return ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, Long.valueOf(ruleId).intValue()).getCode();
    }

    private String extractComment(MinimalVeeReadingValueInfo channelDataInfo) {
        return Optional.ofNullable(channelDataInfo)
                .map(info -> info.commentId)
                .flatMap(id -> readingQualitiesComments.computeIfAbsent(id, this.resourceHelper::getReadingQualityComment))
                .map(ReadingQualityComment::getComment)
                .orElse(channelDataInfo != null ? channelDataInfo.commentValue : null);
    }
}
