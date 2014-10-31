package com.energyict.mdc.issue.datacollection.event;

import static com.elster.jupiter.util.conditions.Where.where;

import java.util.Map;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.google.inject.Injector;

import javax.inject.Inject;

public class UnknownDeviceResolvedEvent extends DataCollectionEvent implements ResolveEvent {
    @Inject
    public UnknownDeviceResolvedEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus, injector);
    }

    @Override
    public void apply(Issue issue) {
        // do nothing, this event shouldn't produce any issues
    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        //do nothing
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("baseIssue.device").isEqualTo(getKoreDevice());
    }
}
