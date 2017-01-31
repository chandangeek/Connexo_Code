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
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

public class UnknownSlaveDeviceEvent extends DataCollectionEvent {
    protected String deviceMRID;

    @Inject
    public UnknownSlaveDeviceEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, communicationTaskService, topologyService, thesaurus, injector);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection) {
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setDeviceMRID(deviceMRID);
        }
    }

    @Override
    public UnknownSlaveDeviceEvent clone() {
        UnknownSlaveDeviceEvent clone = (UnknownSlaveDeviceEvent) super.clone();
        clone.deviceMRID = deviceMRID;
        return clone;
    }

    @Override
    protected void getEventDevice(Map<?, ?> rawEvent) {
        String masterDeviceMrid = (String) rawEvent.get(ModuleConstants.MASTER_DEVICE_IDENTIFIER);
        if (masterDeviceMrid != null) {
            getDeviceService().findDeviceByMrid(masterDeviceMrid).ifPresent(this::setDevice);
        }
    }

    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        // TODO: check what comes here: id, MRID or name, and refactor as name if needed
        this.deviceMRID = (String) rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER);
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("deviceMRID").isEqualTo(deviceMRID);
    }

}
