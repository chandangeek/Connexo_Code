package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

public class UnknownDeviceEvent extends DataCollectionEvent {
    protected String deviceSerialNumber;

    @Inject
    public UnknownDeviceEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus, injector);
    }
    
    @Override
    protected void getEventDevice(Map<?, ?> rawEvent) {
        this.deviceSerialNumber = (String) rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER);
    }

    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription){
        //do nothing
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("deviceSerialNumber").isEqualTo(getDeviceSerialNumber());
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection){
            OpenIssueDataCollection dcIssue = (OpenIssueDataCollection) issue;
            dcIssue.setDeviceSerialNumber(deviceSerialNumber);
        }
    }

    protected String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    @Override
    public UnknownDeviceEvent clone() {
        UnknownDeviceEvent clone = (UnknownDeviceEvent) super.clone();
        clone.deviceSerialNumber = deviceSerialNumber;
        return clone;
    }
}
