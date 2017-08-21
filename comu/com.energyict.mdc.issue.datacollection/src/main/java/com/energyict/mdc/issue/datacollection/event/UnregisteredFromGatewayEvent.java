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
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

public class UnregisteredFromGatewayEvent extends DataCollectionEvent  {

    private String mRID;

    @Inject
    public UnregisteredFromGatewayEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, communicationTaskService, topologyService, thesaurus, injector);
    }

    @Override
    public void apply(Issue issue) {

    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        this.mRID = (String) rawEvent.get("mRID");
        getDeviceService().findDeviceByMrid(mRID).ifPresent(this::setDevice);
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("deviceMRID").isEqualTo(mRID);
    }
}
