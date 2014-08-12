package com.elster.jupiter.issue.datacollection;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;

import com.elster.jupiter.issue.datacollection.impl.AbstractEvent;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

public class DataCollectionEvent extends AbstractEvent {

    protected DataCollectionEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, Thesaurus thesaurus) {
        super(issueService, meteringService, deviceDataService, thesaurus);
    }

    public DataCollectionEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, Thesaurus thesaurus, Map<?, ?> rawEvent) {
        super(issueService, meteringService, deviceDataService, thesaurus, rawEvent);
    }

    @Override
    protected int getNumberOfDevicesWithEvents(Device concentrator) {
        Date start = getLastSuccessfulCommunicationEnd(concentrator);
        int numberOfDevicesWithEvents = 0;
        try {
            numberOfDevicesWithEvents = this.getDeviceDataService().countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(getDescription().getErrorType(), concentrator, Interval.startAt(start));
        } catch (RuntimeException ex){
            LOG.log(Level.WARNING, "Incorrect communication type for concentrator[id={0}]", concentrator.getId());
        }
        return numberOfDevicesWithEvents;
    }

    @Override
    protected AbstractEvent cloneInternal() {
        DataCollectionEvent event = new DataCollectionEvent(getIssueService(), getMeteringService(), getDeviceDataService(), getThesaurus());
        return event;
    }
}
