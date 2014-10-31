package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;

public class DeviceCommunicationFailureEvent extends ConnectionEvent {
    private Optional<ComTaskExecution> comTask;

    @Inject
    public DeviceCommunicationFailureEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, connectionTaskService, thesaurus, injector);
    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        super.wrapInternal(rawEvent, eventDescription);
        String comTaskIdAsStr = (String) rawEvent.get(ModuleConstants.FAILED_TASK_IDS);
        if (!is(comTaskIdAsStr).emptyOrOnlyWhiteSpace()) {
            setComTask(Long.parseLong(comTaskIdAsStr.trim()));
        }
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("comTask").isEqualTo(getComTask().get());
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection) {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setCommunicationTask(getComTask().get());
            dcIssue.setConnectionTask(getConnectionTask().get());
        }
    }

    protected Optional<ComTaskExecution> getComTask() {
        return comTask;
    }

    protected void setComTask(long comTaskId) {
        ComTaskExecution comTaskExecution = getCommunicationTaskService().findComTaskExecution(comTaskId);
        if (comTaskExecution != null) {
            this.comTask = Optional.of(comTaskExecution);
        } else {
            this.comTask = Optional.empty();
            // Todo: throw exception when we can't find the communication task
        }
    }

    @Override
    public DeviceCommunicationFailureEvent clone() {
        DeviceCommunicationFailureEvent clone = (DeviceCommunicationFailureEvent) super.clone();
        clone.comTask = comTask;
        return clone;
    }
}
