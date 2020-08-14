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
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionResolveEventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.DelayedIssueEventHandler;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventType;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Map;

public class UnregisteredFromGatewayEvent extends DataCollectionEvent {

    private long deviceIdentifier;
    private long gatewayIdentifier;

    @Inject
    public UnregisteredFromGatewayEvent(IssueDataCollectionService issueDataCollectionService,
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

    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        this.setDeviceIdentifier(((Number) rawEvent.get("deviceIdentifier")).longValue());
        this.setGatewayIdentifier(((Number) rawEvent.get("gatewayIdentifier")).longValue());
        getDeviceService().findDeviceById(deviceIdentifier).ifPresent(this::setDevice);
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return Condition.TRUE; //not applicable
    }

    @Override
    public EventDescription getIssueCausingEvent() {
        return DataCollectionEventDescription.UNREGISTERED_FROM_GATEWAY;
    }

    @Override
    public EventDescription getIssueResolvingEvent() {
        return DataCollectionResolveEventDescription.REGISTERED_TO_GATEWAY;
    }

    public long getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(long deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public long getGatewayIdentifier() {
        return gatewayIdentifier;
    }

    public void setGatewayIdentifier(long gatewayIdentifier) {
        this.gatewayIdentifier = gatewayIdentifier;
    }

    /**
     * {@link EventType} and {@link DelayedIssueEventHandler}
     * use rule id from this event, first when sending a queue message, second when parsing it.
     * So we need a public getter for a property, corresponding to mentioned in them
     */

    public long getRuleId() {
        return super.getCreationRule();
    }

    @Override
    public boolean isResolveEvent() {
        return false;
    }
}
