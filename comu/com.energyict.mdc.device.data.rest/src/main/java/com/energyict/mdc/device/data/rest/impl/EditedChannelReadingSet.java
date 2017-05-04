/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityComment;
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
import java.util.List;
import java.util.Optional;

class EditedChannelReadingSet {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final TopologyService topologyService;
    private final Channel channel;

    private List<BaseReading> editedReadings = new ArrayList<>();
    private List<BaseReading> editedBulkReadings = new ArrayList<>();
    private List<BaseReading> confirmedReadings = new ArrayList<>();
    private List<BaseReading> estimatedReadings = new ArrayList<>();
    private List<BaseReading> estimatedBulkReadings = new ArrayList<>();
    private List<Instant> removeCandidates = new ArrayList<>();

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

    EditedChannelReadingSet init(List<ChannelDataInfo> channelDataInfos) {
        channelDataInfos.forEach(this::process);
        return this;
    }

    private void process(ChannelDataInfo channelDataInfo) {
        validateLinkedToSlave(channel, Range.closedOpen(Instant.ofEpochMilli(channelDataInfo.interval.start), Instant.ofEpochMilli(channelDataInfo.interval.end)));
        if (!(isToBeConfirmed(channelDataInfo)) && channelDataInfo.value == null && channelDataInfo.collectedValue == null) {
            removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
        } else {
            processCalculatedInfo(channelDataInfo, estimatedReadings, editedReadings);
            processBulkInfo(channelDataInfo, estimatedBulkReadings, editedBulkReadings);
            processConfirmedInfo(channelDataInfo, confirmedReadings);
        }
    }

    private void validateLinkedToSlave(Channel channel, Range<Instant> readingTimeStamp) {
        List<DataLoggerChannelUsage> dataLoggerChannelUsagesForChannels = topologyService.findDataLoggerChannelUsagesForChannels(channel, readingTimeStamp);
        if (!dataLoggerChannelUsagesForChannels.isEmpty()) {
            throw this.exceptionFactory.newException(MessageSeeds.CANNOT_ADDEDITREMOVE_CHANNEL_VALUE_WHEN_LINKED_TO_SLAVE);
        }
    }

    private void processCalculatedInfo(ChannelDataInfo channelDataInfo, List<BaseReading> estimatedReadings, List<BaseReading> editedReadings) {
        if (channelDataInfo.value != null) {
            BaseReadingImpl baseReading = channelDataInfo.createNew();
            Optional<ReadingQualityComment> readingQualityComment =
                    resourceHelper.getReadingQualityComment(channelDataInfo.mainValidationInfo == null ? 0 : channelDataInfo.mainValidationInfo.commentId);
            if (channelDataInfo.mainValidationInfo != null && channelDataInfo.mainValidationInfo.ruleId != 0) {
                baseReading.addQuality("2.8." + channelDataInfo.mainValidationInfo.ruleId, this.extractComment(readingQualityComment));
                estimatedReadings.add(baseReading);
            } else {
                baseReading.addQuality("2.7.0", this.extractComment(readingQualityComment));
                editedReadings.add(baseReading);
            }
        }
    }

    private void processBulkInfo(ChannelDataInfo channelDataInfo, List<BaseReading> estimatedBulkReadings, List<BaseReading> editedBulkReadings) {
        if (channelDataInfo.collectedValue != null) {
            BaseReadingImpl baseReading = channelDataInfo.createNewBulk();
            Optional<ReadingQualityComment> readingQualityComment =
                    resourceHelper.getReadingQualityComment(channelDataInfo.bulkValidationInfo == null ? 0 : channelDataInfo.bulkValidationInfo.commentId);
            if (channelDataInfo.bulkValidationInfo != null && channelDataInfo.bulkValidationInfo.ruleId != 0) {
                baseReading.addQuality("2.8." + channelDataInfo.bulkValidationInfo.ruleId, this.extractComment(readingQualityComment));
                estimatedBulkReadings.add(baseReading);
            } else {
                baseReading.addQuality("2.7.0", this.extractComment(readingQualityComment));
                editedBulkReadings.add(baseReading);
            }
        }
    }

    private void processConfirmedInfo(ChannelDataInfo channelDataInfo, List<BaseReading> confirmedReadings) {
        if (isToBeConfirmed(channelDataInfo)) {
            confirmedReadings.add(channelDataInfo.createConfirm());
        }
    }

    private boolean isToBeConfirmed(ChannelDataInfo channelDataInfo) {
        return channelDataInfo.mainValidationInfo != null && channelDataInfo.mainValidationInfo.isConfirmed ||
                channelDataInfo.bulkValidationInfo != null && channelDataInfo.bulkValidationInfo.isConfirmed;
    }

    private String extractComment(Optional<ReadingQualityComment> readingQualityComment) {
        return readingQualityComment.map(ReadingQualityComment::getComment).orElse(null);
    }
}
