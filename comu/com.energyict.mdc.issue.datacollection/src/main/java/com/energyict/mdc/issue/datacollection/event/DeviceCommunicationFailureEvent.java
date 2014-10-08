package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;

import javax.inject.Inject;
import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

public class DeviceCommunicationFailureEvent extends DataCollectionEvent {

    private long comTaskId;

    @Inject
    public DeviceCommunicationFailureEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus);
    }

    public void wrap(Map<?, ?> rawEvent, DataCollectionEventDescription eventDescription){
        super.wrap(rawEvent, eventDescription);
        String comTaskIdAsStr = (String) rawEvent.get(ModuleConstants.FAILED_TASK_IDS);
        if (!is(comTaskIdAsStr).emptyOrOnlyWhiteSpace()){
            comTaskId = Long.parseLong(comTaskIdAsStr.trim());
        }
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection){
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setCommunicationTask(getCommunicationTaskService().findComTaskExecution(comTaskId));
        }
    }
}
