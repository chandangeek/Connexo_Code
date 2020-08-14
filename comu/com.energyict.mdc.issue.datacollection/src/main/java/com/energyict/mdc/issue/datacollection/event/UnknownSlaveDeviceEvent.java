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
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

public class UnknownSlaveDeviceEvent extends DataCollectionEvent {
    protected String slaveDeviceIdentification;

    @Inject
    public UnknownSlaveDeviceEvent(IssueDataCollectionService issueDataCollectionService,
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
                communicationTaskService,
                topologyService,
                thesaurus,
                eventService,
                timeService,
                clock,
                injector,
                issueService);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection) {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setDeviceIdentification(slaveDeviceIdentification);
        }
    }

    @Override
    public UnknownSlaveDeviceEvent clone() {
        UnknownSlaveDeviceEvent clone = (UnknownSlaveDeviceEvent) super.clone();
        clone.slaveDeviceIdentification = slaveDeviceIdentification;
        return clone;
    }

    @Override
    public EventDescription getIssueCausingEvent() {
        return DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE;
    }

    @Override
    public EventDescription getIssueResolvingEvent() {
        return DataCollectionResolveEventDescription.UNKNOWN_OUTBOUND_DEVICE_EVENT_AUTO_RESOLVE;
    }

    @Override
    protected void getEventDevice(Map<?, ?> rawEvent) {
        String masterDeviceMrid = (String) rawEvent.get(ModuleConstants.MASTER_DEVICE_IDENTIFIER);
        if (masterDeviceMrid != null) {
            getDeviceService().findDeviceByMrid(masterDeviceMrid).ifPresent(this::setDevice);
        }
    }

    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        this.slaveDeviceIdentification = (String) rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER); // Which should be the 'toString()' of the slave DeviceIdentifier
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("deviceMRID").isEqualTo(slaveDeviceIdentification);
    }

}
