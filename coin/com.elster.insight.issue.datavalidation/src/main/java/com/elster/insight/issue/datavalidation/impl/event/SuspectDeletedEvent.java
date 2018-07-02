/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.insight.issue.datavalidation.IssueDataValidationService;
import com.elster.insight.issue.datavalidation.OpenIssueDataValidation;
import com.elster.insight.issue.datavalidation.impl.MessageSeeds;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.Map;

public class SuspectDeletedEvent extends DataValidationEvent {

    private Instant readingTimestamp;

    @Inject
    public SuspectDeletedEvent(Thesaurus thesaurus, ServerMeteringService meteringService, IssueDataValidationService issueDataValidationService, IssueService issueService) {
        super(thesaurus, meteringService, issueDataValidationService, issueService);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataValidation) {
            OpenIssueDataValidation dataValidationIssue = (OpenIssueDataValidation) issue;
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
