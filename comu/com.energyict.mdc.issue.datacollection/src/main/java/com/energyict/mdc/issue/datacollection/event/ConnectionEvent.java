package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.google.common.base.Optional;
import com.google.inject.Injector;

import java.util.Map;

import static com.elster.jupiter.util.Checks.is;
import static com.elster.jupiter.util.conditions.Where.where;

public abstract class ConnectionEvent extends DataCollectionEvent{
    private Optional<ConnectionTask> connectionTask;
    private final ConnectionTaskService connectionTaskService;

    public ConnectionEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus, injector);
        this.connectionTaskService = connectionTaskService;
    }

    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription){
        String connectionTaskIdAsStr = (String) rawEvent.get(ModuleConstants.CONNECTION_TASK_ID);
        if (!is(connectionTaskIdAsStr).emptyOrOnlyWhiteSpace()){
            setConnectionTask(Long.parseLong(connectionTaskIdAsStr.trim()));
        }
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("connectionTask").isEqualTo(getConnectionTask());
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection){
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setConnectionTask(getConnectionTask().get());
        }
    }

    protected ConnectionTaskService getConnectionTaskService() {
        return connectionTaskService;
    }

    protected Optional<ConnectionTask> getConnectionTask() {
        return this.connectionTask;
    }

    protected void setConnectionTask(long connectionTaskId) {
        this.connectionTask = getConnectionTaskService().findConnectionTask(connectionTaskId);
        // TODO throw exception when we can't find the connection task
    }

    @Override
    public ConnectionEvent clone() {
        ConnectionEvent clone = (ConnectionEvent) super.clone();
        clone.connectionTask = connectionTask;
        return clone;
    }
}
