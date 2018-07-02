/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.event;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.insight.issue.datavalidation.IssueDataValidationService;
import com.elster.insight.issue.datavalidation.OpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.impl.MessageSeeds;

import com.google.common.collect.Range;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.Map;

public class CannotEstimateDataEvent extends DataValidationEvent {

    private Instant startTime;
    private Instant endTime;

    @Inject
    public CannotEstimateDataEvent(Thesaurus thesaurus, ServerMeteringService meteringService, IssueDataValidationService issueDataValidationService, IssueService issueService) {
        super(thesaurus, meteringService, issueDataValidationService, issueService);
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.startTime = Instant.ofEpochMilli(((Number) jsonPayload.get("startTime")).longValue());
            this.endTime = Instant.ofEpochMilli(((Number) jsonPayload.get("endTime")).longValue());
            this.channelId = ((Number) jsonPayload.get("channelId")).longValue();
            this.readingType = (String) jsonPayload.get("readingType");
        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataValidation) {
            OpenIssueDataValidation dataValidationIssue = (OpenIssueDataValidation) issue;
            Channel channel = findChannel().get();
            ReadingType readingType = findReadingType().get();
            channel.getCimChannel(readingType).get()
                    .findReadingQualities()
                    .ofQualitySystem(QualityCodeSystem.MDM)
                    .ofQualityIndex(QualityCodeIndex.SUSPECT)
                    .inTimeInterval(Range.closed(startTime, endTime))
                    .actual()
                    .collect()
                    .forEach(rq -> dataValidationIssue.addNotEstimatedBlock(channel, readingType, rq.getReadingTimestamp()));
        }
    }
}
