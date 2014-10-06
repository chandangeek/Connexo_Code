package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.impl.AbstractEvent;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

public class DataCollectionEvent extends AbstractEvent {

    protected DataCollectionEvent(IssueService issueService, MeteringService meteringService, CommunicationTaskService communicationTaskService, DeviceService deviceService, Thesaurus thesaurus) {
        super(issueService, meteringService, communicationTaskService, deviceService, thesaurus);
    }

    public DataCollectionEvent(IssueService issueService, MeteringService meteringService, CommunicationTaskService communicationTaskService, DeviceService deviceService, Thesaurus thesaurus, Map<?, ?> rawEvent) {
        super(issueService, meteringService, communicationTaskService, deviceService, thesaurus, rawEvent);
    }

    @Override
    protected int getNumberOfDevicesWithEvents(Device concentrator) {
        Date start = getLastSuccessfulCommunicationEnd(concentrator);
        int numberOfDevicesWithEvents = 0;
        try {
            numberOfDevicesWithEvents = this.getCommunicationTaskService().countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(getDescription().getErrorType(), concentrator, Interval.startAt(start));
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Incorrect communication type for concentrator[id={0}]", concentrator.getId());
        }
        return numberOfDevicesWithEvents;
    }

    @Override
    protected AbstractEvent cloneInternal() {
        return new DataCollectionEvent(getIssueService(), getMeteringService(), getCommunicationTaskService(), getDeviceService(), getThesaurus());
    }

}