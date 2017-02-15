/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.elster.jupiter.util.Checks.is;

public class DeviceCommunicationFailureResolvedEvent extends DeviceCommunicationFailureEvent {

    @Inject
    public DeviceCommunicationFailureResolvedEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, topologyService, communicationTaskService, connectionTaskService, thesaurus, injector);
    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription){
        String comTaskIdAsStr = (String) rawEvent.get(ModuleConstants.SUCCESS_TASK_IDS);
        if (!is(comTaskIdAsStr).emptyOrOnlyWhiteSpace()){
            setComTaskId(Long.parseLong(comTaskIdAsStr.trim()));
        }
    }

    @Override
    public void apply(Issue issue) {
        // do nothing, this event shouldn't produce any issues
    }

    public boolean isResolveEvent(){
        return true;
    }

}