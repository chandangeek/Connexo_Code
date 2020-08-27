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

public class UnableToConnectEvent extends ConnectionEvent {
    @Inject
    public UnableToConnectEvent(IssueDataCollectionService issueDataCollectionService,
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
        return super.getConditionForExistingIssue().and(where("comSession.successIndicator").isEqualTo(ComSession.SuccessIndicator.SetupError));
    }

    @Override
    public EventDescription getIssueCausingEvent() {
        return DataCollectionEventDescription.UNABLE_TO_CONNECT;
    }

    @Override
    public EventDescription getIssueResolvingEvent() {
        return DataCollectionResolveEventDescription.UNABLE_TO_CONNECT_AUTO_RESOLVE;
    }

}