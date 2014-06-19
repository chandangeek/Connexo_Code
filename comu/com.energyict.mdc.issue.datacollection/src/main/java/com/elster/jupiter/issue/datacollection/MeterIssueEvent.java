package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.datacollection.impl.AbstractEvent;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.UnableToCreateEventException;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MeterIssueEvent extends AbstractEvent {
    private String eventIdentifier;
    private EndDeviceEventRecord eventRecord;

    public MeterIssueEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, Thesaurus thesaurus, Map<?, ?> rawEvent) {
        super(issueService, meteringService, deviceDataService, thesaurus, rawEvent);
    }

    @Override
    protected void init(Map<?, ?> rawEvent) {
        super.init(rawEvent);
        /* ID of event
        eventIdentifier = (String) rawEvent.get(ModuleConstants.EVENT_IDENTIFIER);
        */
        long timestamp = (Long) rawEvent.get(ModuleConstants.EVENT_TIMESTAMP);
        List<EndDeviceEventRecord> deviceEvents = getDevice().getDeviceEvents(new Interval(new Date(timestamp), new Date(timestamp)));
        if (deviceEvents.size() != 1){
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_EVENT_IDENTIFIER);
        }
        eventRecord = deviceEvents.get(0);
    }

    @SuppressWarnings("unused")
    // Used in Drool's rule (@see BasicMeterRuleTemplate)
    public String getEndDeviceEventType(){
        return eventRecord.getEventType().getMRID();
    }

    @Override
    public String getEventType() {
        return eventRecord.getEventType().getMRID();
    }

    @Override
    protected int getNumberOfEvents(Device concentrator) {
        Date start = getLastSuccessfulCommunicationEnd(concentrator);
        return concentrator.countNumberOfEndDeviceEvents(Arrays.asList(eventRecord.getEventType()), new Interval(start, null));
    }
}
