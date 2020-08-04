/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.time.Clock;

public class ConnectionLostResolvedEvent extends ConnectionLostEvent {
    @Inject
    public ConnectionLostResolvedEvent(IssueDataCollectionService issueDataCollectionService,
                                       MeteringService meteringService,
                                       DeviceService deviceService,
                                       TopologyService topologyService,
                                       CommunicationTaskService communicationTaskService,
                                       ConnectionTaskService connectionTaskService,
                                       Thesaurus thesaurus,
                                       Injector injector,
                                       EventService eventService,
                                       TimeService timeService,
                                       Clock clock,
                                       IssueService issueService) {
        super(issueDataCollectionService,
                meteringService,
                deviceService,
                topologyService,
                communicationTaskService,
                connectionTaskService,
                thesaurus,
                injector,
                timeService,
                clock,
                eventService,
                issueService);
    }

    @Override
    public void apply(Issue issue) {
        // do nothing, this event shouldn't produce any issues
    }

    public boolean isResolveEvent() {
        return true;
    }

}