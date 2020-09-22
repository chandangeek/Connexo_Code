/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.time.Clock;

import static com.elster.jupiter.util.conditions.Where.where;

public class ConnectionLostEvent extends ConnectionEvent {
    @Inject
    public ConnectionLostEvent(IssueDataCollectionService issueDataCollectionService,
                               MeteringService meteringService, DeviceService deviceService,
                               TopologyService topologyService,
                               CommunicationTaskService communicationTaskService,
                               ConnectionTaskService connectionTaskService,
                               Thesaurus thesaurus,
                               Injector injector,
                               TimeService timeService,
                               Clock clock,
                               EventService eventService,
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
                eventService,
                clock,
                issueService);
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return super.getConditionForExistingIssue().and(where("comSession.successIndicator").isEqualTo(ComSession.SuccessIndicator.Broken));
    }

    @Override
    public EventDescription getIssueCausingEvent() {
        return DataCollectionEventDescription.CONNECTION_LOST;
    }

    @Override
    public EventDescription getIssueResolvingEvent() {
        return DataCollectionResolveEventDescription.CONNECTION_LOST_AUTO_RESOLVE;
    }

}