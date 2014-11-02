package com.energyict.mdc.issue.datacollection.event;

import static com.elster.jupiter.util.conditions.Where.where;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.google.inject.Injector;

import javax.inject.Inject;

public class UnableToConnectEvent extends ConnectionEvent {
    @Inject
    public UnableToConnectEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, connectionTaskService, thesaurus, injector);
    }
    
    
    @Override
    protected Condition getConditionForExistingIssue() {
        return super.getConditionForExistingIssue().and(where("comSession.successIndicator").isEqualTo(ComSession.SuccessIndicator.SetupError));
    }
}
