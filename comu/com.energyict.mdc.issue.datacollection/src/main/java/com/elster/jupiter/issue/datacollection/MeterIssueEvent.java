package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.datacollection.impl.AbstractEvent;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;

import java.util.Map;

public class MeterIssueEvent extends AbstractEvent {
    private String eventIdentifier;

    public MeterIssueEvent(IssueService issueService, MeteringService meteringService, Map<?, ?> rawEvent) {
        super(issueService, meteringService, rawEvent);
    }

    @Override
    protected void init(Map<?, ?> rawEvent) {
        super.init(rawEvent);

        eventIdentifier = (String) rawEvent.get(ModuleConstants.EVENT_IDENTIFIER);
    }

    public String getEndDeviceEventType(){
        //TODO use correct API
        return "*." + ("1".equalsIgnoreCase(eventIdentifier) ? "26.0.85": "36.116.85");
    }
}
