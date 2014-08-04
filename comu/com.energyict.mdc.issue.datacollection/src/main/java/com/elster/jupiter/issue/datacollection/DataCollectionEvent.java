package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.datacollection.impl.AbstractEvent;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

public class DataCollectionEvent extends AbstractEvent {

    private final TaskHistoryService taskHistoryService;

    protected DataCollectionEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, TaskHistoryService taskHistoryService, Thesaurus thesaurus) {
        super(issueService, meteringService, deviceDataService, thesaurus);
        this.taskHistoryService = taskHistoryService;
    }

    public DataCollectionEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, TaskHistoryService taskHistoryService, Thesaurus thesaurus, Map<?, ?> rawEvent) {
        super(issueService, meteringService, deviceDataService, thesaurus, rawEvent);
        this.taskHistoryService = taskHistoryService;
    }

    @Override
    protected int getNumberOfEvents(Device concentrator) {
        Date start = getLastSuccessfulCommunicationEnd(concentrator);
        int numberOfEvents = 0;
        try {
            numberOfEvents = taskHistoryService.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(getDescription().getErrorType(), concentrator, Interval.startAt(start));
        } catch (RuntimeException ex){
            LOG.log(Level.WARNING, "Incorrect communication type for concentrator[id={0}]", concentrator.getId());
        }
        return numberOfEvents;
    }

    @Override
    protected AbstractEvent cloneInternal() {
        DataCollectionEvent event = new DataCollectionEvent(getIssueService(), getMeteringService(), getDeviceDataService(), taskHistoryService, getThesaurus());
        return event;
    }
}
