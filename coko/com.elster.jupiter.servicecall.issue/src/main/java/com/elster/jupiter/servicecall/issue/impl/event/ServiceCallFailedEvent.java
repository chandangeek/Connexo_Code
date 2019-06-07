/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;

import java.util.Map;
import java.util.Optional;

public class ServiceCallFailedEvent extends ServiceCallEvent {

    public ServiceCallFailedEvent(Thesaurus thesaurus, IssueServiceCallService issueServiceCallService, IssueService issueService) {
        super(thesaurus, issueServiceCallService, issueService);
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {
//        LOG.info("servicecall failed issue");
    }

    @Override
    void init(Map<?, ?> jsonPayload) {

    }
}
