/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.FiltrableByComTask;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class ConnectionEvent extends DataCollectionEvent implements Cloneable, FiltrableByComTask {
    private Optional<Long> connectionTaskId;
    private Optional<Long> comSessionId;
    private final ConnectionTaskService connectionTaskService;

    public ConnectionEvent(IssueDataCollectionService issueDataCollectionService,
                           MeteringService meteringService,
                           DeviceService deviceService,
                           TopologyService topologyService,
                           CommunicationTaskService communicationTaskService,
                           ConnectionTaskService connectionTaskService,
                           Thesaurus thesaurus,
                           Injector injector,
                           TimeService timeService,
                           EventService eventService,
                           Clock clock,
                           IssueService issueService) {
        super(issueDataCollectionService, meteringService, deviceService, communicationTaskService, topologyService, thesaurus, eventService, timeService, clock, injector, issueService);
        this.connectionTaskService = connectionTaskService;
    }

    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        this.connectionTaskId = getLong(rawEvent, ModuleConstants.CONNECTION_TASK_ID);
        this.comSessionId = getLong(rawEvent, ModuleConstants.COM_SESSION_ID);
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("connectionTask.id").isEqualTo(connectionTaskId.get()).and(where("comTask").isNull());
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection) {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setConnectionTask(getConnectionTask().get());
            dcIssue.setComSession(getComSession().get());
        }
    }

    protected ConnectionTaskService getConnectionTaskService() {
        return connectionTaskService;
    }

    protected Optional<ConnectionTask> getConnectionTask() {
        return connectionTaskId.map(cti -> getConnectionTaskService().findConnectionTask(cti)).orElse(Optional.empty());
    }

    protected Optional<ComSession> getComSession() {
        return comSessionId.map(csi -> getConnectionTaskService().findComSession(csi)).orElse(Optional.empty());
    }

    @Override
    public ConnectionEvent clone() {
        ConnectionEvent clone = (ConnectionEvent) super.clone();
        clone.connectionTaskId = connectionTaskId;
        clone.comSessionId = comSessionId;
        return clone;
    }

    @Override
    public boolean matchesByComTask(List<HasId> comTasks) {
        Optional<ComSession> comSession = this.getComSession();
        if (comSession.isPresent()) {
            List<ComTaskExecutionSession> comTaskExecutionSessions = comSession.get().getComTaskExecutionSessions();
            if (comTaskExecutionSessions != null && !comTaskExecutionSessions.isEmpty()) {
                final List<Long> comTaskIds = comTasks.stream().map(e -> e.getId()).collect(Collectors.toList());
                final List<ComTask> validComTasks = comTaskExecutionSessions.stream()
                        .filter(e -> ComTaskExecutionSession.SuccessIndicator.unSuccessful()
                                .contains(e.getSuccessIndicator()))
                        .map(e -> e.getComTask()).filter(e -> !comTaskIds.contains(e.getId()))
                        .collect(Collectors.toList());
                if (validComTasks.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass() + "{" +
                "connectionTaskId=" + connectionTaskId +
                ", comSessionId=" + comSessionId +
                ", deviceId="  + super.getDevice().getmRID() + "}";
    }
}
