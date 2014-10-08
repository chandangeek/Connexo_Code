package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;

import java.util.Map;

public abstract class UnknownDeviceEvent extends DataCollectionEvent {
    protected String deviceSerialNumber;

    protected UnknownDeviceEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus);
    }

    public void wrap(Map<?, ?> rawEvent, DataCollectionEventDescription eventDescription){
        super.wrap(rawEvent, eventDescription);
        this.deviceSerialNumber = (String) rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection){
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setDeviceSerialNumber(deviceSerialNumber);
        }
    }
}
