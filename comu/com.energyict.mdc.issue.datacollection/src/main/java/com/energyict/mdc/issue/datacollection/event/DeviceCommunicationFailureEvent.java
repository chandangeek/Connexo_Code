/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
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
    private Optional<Long> comTaskId = Optional.empty();

    @Inject
    public DeviceCommunicationFailureEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, topologyService, communicationTaskService, connectionTaskService, thesaurus, injector);
    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        super.wrapInternal(rawEvent, eventDescription);
        String comTaskIdAsStr = (String) rawEvent.get(ModuleConstants.FAILED_TASK_IDS);
        if (!is(comTaskIdAsStr).emptyOrOnlyWhiteSpace()) {
            setComTaskId(Long.parseLong(comTaskIdAsStr.trim()));
        }
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("comTask.id").isEqualTo(comTaskId.get());
    }

    @Override
    public void apply(Issue issue) {
        super.apply(issue);
        if (issue instanceof OpenIssueDataCollection) {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setCommunicationTask(getComTask().get());
        }
    }

    protected Optional<ComTaskExecution> getComTask() {
        return comTaskId.map(cti -> getCommunicationTaskService().findComTaskExecution(cti)).orElse(Optional.empty());
    }

    @Override
    public DeviceCommunicationFailureEvent clone() {
        DeviceCommunicationFailureEvent clone = (DeviceCommunicationFailureEvent) super.clone();
        clone.comTaskId = comTaskId;
        return clone;
    }

    protected void setComTaskId(Long comTaskId) {
        this.comTaskId = Optional.of(comTaskId);
    }
}