package com.energyict.mdc.issue.datacollection.event;

import static com.elster.jupiter.util.conditions.Where.where;

import java.util.Map;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.google.inject.Injector;

public class UnknownSlaveDeviceEvent extends DataCollectionEvent {
    protected String deviceMRID;

    @Inject
    public UnknownSlaveDeviceEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus, injector);
    }
    
    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection){
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
            Device masterDevice = getDeviceService().findByUniqueMrid(masterDeviceMrid);
            if (masterDevice != null) {
                setDevice(masterDevice);
                setEndDevice(findEndDeviceByMdcDevice());
            }
        }
    }

    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription){
        this.deviceMRID = (String) rawEvent.get(ModuleConstants.DEVICE_IDENTIFIER);
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("deviceMRID").isEqualTo(deviceMRID);
    }
}
