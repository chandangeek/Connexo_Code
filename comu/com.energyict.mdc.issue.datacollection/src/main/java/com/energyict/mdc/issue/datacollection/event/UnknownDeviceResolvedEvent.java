/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class UnknownDeviceResolvedEvent extends UnknownSlaveDeviceEvent {
    @Inject
    public UnknownDeviceResolvedEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, topologyService, communicationTaskService, thesaurus, injector);
    }

    @Override
    public void apply(Issue issue) {
        // do nothing, this event shouldn't produce any issues
    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        //do nothing
    }

    @Override
    protected void getEventDevice(Map<?, ?> rawEvent) {
        Optional<Long> deviceId = getLong(rawEvent, "id");
        if (deviceId.isPresent()) {
            getDeviceService().findDeviceById(deviceId.get()).ifPresent(this::setDevice);
        }
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("deviceMRID").isEqualTo(getDevice().getmRID());
    }

    public boolean isResolveEvent(){
        return true;
    }

}