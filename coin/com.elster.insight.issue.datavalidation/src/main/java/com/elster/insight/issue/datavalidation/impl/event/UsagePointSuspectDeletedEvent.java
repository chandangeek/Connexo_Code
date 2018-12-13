/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.insight.issue.datavalidation.UsagePointOpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.impl.MessageSeeds;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.Map;

public class UsagePointSuspectDeletedEvent extends UsagePointDataValidationEvent {

    private Instant readingTimestamp;

    @Inject
    public UsagePointSuspectDeletedEvent(Thesaurus thesaurus, MeteringService meteringService, UsagePointIssueDataValidationService usagePointIssueDataValidationService, IssueService issueService) {
        super(thesaurus, meteringService, usagePointIssueDataValidationService, issueService);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof UsagePointOpenIssueDataValidation) {
            UsagePointOpenIssueDataValidation dataValidationIssue = (UsagePointOpenIssueDataValidation) issue;
            dataValidationIssue.removeNotEstimatedBlock(findChannel().get(), findReadingType().get(), readingTimestamp);
        }
    }

    @Override
    void init(Map<?, ?> jsonPayload) {
        try {
            this.readingTimestamp = Instant.ofEpochMilli((Long) jsonPayload.get("readingTimestamp"));
            this.channelId = ((Number) jsonPayload.get("channelId")).longValue();
            this.readingType = (String) jsonPayload.get("readingType");
        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }
}
