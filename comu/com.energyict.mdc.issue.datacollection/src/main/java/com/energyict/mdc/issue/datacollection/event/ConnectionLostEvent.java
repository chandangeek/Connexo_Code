/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import com.google.inject.Injector;

import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

public class ConnectionLostEvent extends ConnectionEvent {
    @Inject
    public ConnectionLostEvent(IssueDataCollectionService issueDataCollectionService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, meteringService, deviceService, topologyService, communicationTaskService, connectionTaskService, thesaurus, injector);
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return super.getConditionForExistingIssue().and(where("comSession.successIndicator").isEqualTo(ComSession.SuccessIndicator.Broken));
    }

}