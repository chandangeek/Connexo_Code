/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.BaseReadingImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

class EditedChannelReadingSet {

    private final ResourceHelper resourceHelper;

    private final List<BaseReading> editedReadings = new ArrayList<>();
    private final List<BaseReading> estimatedReadings = new ArrayList<>();
    private final List<BaseReading> confirmedReadings = new ArrayList<>();
    private final Set<Instant> removeCandidates = new HashSet<>();
    private final Map<Long, Optional<ReadingQualityComment>> readingQualitiesComments = new HashMap<>();

    EditedChannelReadingSet(ResourceHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
    }

    EditedChannelReadingSet init(List<OutputChannelDataInfo> channelDataInfos) {
        channelDataInfos.forEach(this::processInfo);
        return this;
    }

    List<BaseReading> getEditedReadings() {
        return Collections.unmodifiableList(editedReadings);
    }

    List<BaseReading> getEstimatedReadings() {
        return Collections.unmodifiableList(estimatedReadings);
    }

    List<BaseReading> getConfirmedReadings() {
        return Collections.unmodifiableList(confirmedReadings);
    }

    Set<Instant> getRemoveCandidates() {
        return Collections.unmodifiableSet(removeCandidates);
    }

    Optional<Instant> getFirstEditedReadingTime() {
        return Stream.of(
                removeCandidates.stream(),
                editedReadings.stream().map(BaseReading::getTimeStamp),
                estimatedReadings.stream().map(BaseReading::getTimeStamp),
                confirmedReadings.stream().map(BaseReading::getTimeStamp))
                .flatMap(Function.identity())
                .min(Comparator.naturalOrder());
    }

    private void processInfo(OutputChannelDataInfo channelDataInfo) {
        if (!isToBeConfirmed(channelDataInfo) && channelDataInfo.value == null) {
            this.removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
        } else {
            processValue(channelDataInfo);
            processConfirmedInfo(channelDataInfo);
        }
    }

    private boolean isToBeConfirmed(OutputChannelDataInfo channelDataInfo) {
        return Boolean.TRUE.equals(channelDataInfo.isConfirmed);
    }

    private void processValue(OutputChannelDataInfo channelDataInfo) {
        if (channelDataInfo.value != null) {
            BaseReadingImpl reading = channelDataInfo.createNew();
            String comment = extractComment(channelDataInfo);
            if (channelDataInfo.isProjected) {
                reading.addQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.PROJECTEDGENERIC).getCode(), comment);
                this.editedReadings.add(reading);
            } else if (channelDataInfo.ruleId != 0) {
                reading.addQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, Long.valueOf(channelDataInfo.ruleId).intValue()).getCode(), comment);
                this.estimatedReadings.add(reading);
            } else {
                reading.addQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC).getCode(), comment);
                this.editedReadings.add(reading);
            }
        }
    }

    private void processConfirmedInfo(OutputChannelDataInfo channelDataInfo) {
        if (isToBeConfirmed(channelDataInfo)) {
            this.confirmedReadings.add(channelDataInfo.createConfirm());
        }
    }

    private String extractComment(OutputChannelDataInfo channelDataInfo) {
        return Optional.ofNullable(channelDataInfo)
                .map(info -> info.commentId)
                .flatMap(id -> readingQualitiesComments.computeIfAbsent(id, this.resourceHelper::getReadingQualityComment))
                .map(ReadingQualityComment::getComment)
                .orElse(channelDataInfo != null ? channelDataInfo.commentValue : null);
    }
}
