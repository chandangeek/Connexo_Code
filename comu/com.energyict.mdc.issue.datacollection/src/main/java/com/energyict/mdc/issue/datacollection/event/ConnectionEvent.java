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
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.google.inject.Injector;

import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class ConnectionEvent extends DataCollectionEvent implements Cloneable {
    private Optional<ConnectionTask> connectionTask;
    private Optional<ComSession> comSession;
    private final ConnectionTaskService connectionTaskService;

    public ConnectionEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus, injector);
        this.connectionTaskService = connectionTaskService;
    }

    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription){
        Optional<Long> connectionTaskId = getLong(rawEvent, ModuleConstants.CONNECTION_TASK_ID);
        if (connectionTaskId.isPresent()){
            setConnectionTask(connectionTaskId.get());
        }

        Optional<Long> comSessionId = getLong(rawEvent, ModuleConstants.COM_SESSION_ID);
        if (comSessionId.isPresent()){
            setComSession(comSessionId.get());
        }
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("connectionTask").isEqualTo(getConnectionTask().get()).and(where("comTask").isNull());
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection){
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setConnectionTask(getConnectionTask().get());
            dcIssue.setComSession(getComSession().get());
        }
    }

    protected ConnectionTaskService getConnectionTaskService() {
        return connectionTaskService;
    }

    protected Optional<ConnectionTask> getConnectionTask() {
        return this.connectionTask;
    }
    
    protected Optional<ComSession> getComSession() {
        return comSession;
    }

    protected void setConnectionTask(long connectionTaskId) {
        this.connectionTask = getConnectionTaskService().findConnectionTask(connectionTaskId);
    }
    
    protected void setComSession(long comSessionId) {
        this.comSession = getConnectionTaskService().findComSession(comSessionId);
    }

    @Override
    public ConnectionEvent clone() {
        ConnectionEvent clone = (ConnectionEvent) super.clone();
        clone.connectionTask = connectionTask;
        clone.comSession = comSession;
        return clone;
    }
}
