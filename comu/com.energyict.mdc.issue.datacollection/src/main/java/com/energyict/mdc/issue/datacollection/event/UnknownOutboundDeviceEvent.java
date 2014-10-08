package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import javax.inject.Inject;

public class UnknownOutboundDeviceEvent extends UnknownDeviceEvent {

    @Inject
    public UnknownOutboundDeviceEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus);
    }
}
