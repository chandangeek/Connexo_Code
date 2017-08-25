/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.energyict.mdc.issue.datacollection.impl.event.EventType;

import com.google.inject.Injector;

import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class UnregisteredFromGatewayDelayedEvent extends DataCollectionEvent {

    private final String deviceMrid;
    private String lastGatewayMrid;

    public UnregisteredFromGatewayDelayedEvent(Device device, Optional<Device> gateway, IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, TopologyService topologyService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, communicationTaskService, topologyService, thesaurus, injector);
        setDevice(device);
        this.deviceMrid = device.getmRID();
        gateway.ifPresent(g -> lastGatewayMrid = g.getmRID());
    }

    @Override
    public String getEventType() {
        return EventType.UNREGISTERED_FROM_GATEWAY_DELAYED.name();
    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        //do nothing, this is never called (as this is a special case)
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("deviceMRID").isEqualTo(deviceMrid);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection) {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            if (getEndDevice().isPresent()) {
                dcIssue.setDevice(getEndDevice().get());
                dcIssue.setDeviceIdentification(deviceMrid);
                dcIssue.setLastGatewayIdentification(lastGatewayMrid);
            }
        }
    }
}
