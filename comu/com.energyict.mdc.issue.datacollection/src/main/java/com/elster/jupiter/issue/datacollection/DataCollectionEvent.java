package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.datacollection.impl.AbstractEvent;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;

import java.util.Map;

public class DataCollectionEvent extends AbstractEvent {
    public DataCollectionEvent(IssueService issueService, MeteringService meteringService, Map<?, ?> rawEvent) {
        super(issueService, meteringService, rawEvent);
    }
}
