/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Map;

public class UnknownInboundDeviceEvent extends UnknownSlaveDeviceEvent {

    @Inject
    public UnknownInboundDeviceEvent(IssueDataCollectionService issueDataCollectionService,
                                     MeteringService meteringService,
                                     DeviceService deviceService,
                                     TopologyService topologyService,
                                     CommunicationTaskService communicationTaskService,
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
                thesaurus,
                injector,
                timeService,
                eventService,
                clock,
                issueService);
    }

    @Override
    protected void getEventDevice(Map<?, ?> rawEvent) {
        //nothing to do because the event doesn't contain info about device, only mrid which is parsed in super.wrapInternal(...)
    }

    @Override
    public EventDescription getIssueCausingEvent() {
        return DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE;
    }

    @Override
    public EventDescription getIssueResolvingEvent() {
        return DataCollectionResolveEventDescription.UNKNOWN_INBOUND_DEVICE_EVENT_AUTO_RESOLVE;
    }
}