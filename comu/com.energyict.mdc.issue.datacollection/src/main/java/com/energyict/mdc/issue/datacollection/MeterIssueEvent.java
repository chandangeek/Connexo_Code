package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.impl.AbstractEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.UnableToCreateEventException;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MeterIssueEvent extends AbstractEvent {
    private String endDeviceEventType;
    private EndDeviceEventRecord eventRecord;

    @Inject
    public MeterIssueEvent(IssueService issueService, MeteringService meteringService, DeviceService deviceService, Thesaurus thesaurus) {
        super(issueService, meteringService, deviceService, thesaurus);
    }

    @Override
    public void init(Map<?, ?> rawEvent,  DataCollectionEventDescription eventDescription) {
        super.init(rawEvent, eventDescription);

        long timestamp = getLong(rawEvent, ModuleConstants.EVENT_TIMESTAMP);
        List<EndDeviceEventRecord> deviceEvents = getKoreDevice().getDeviceEvents(new Interval(new Date(timestamp), new Date(timestamp)));
        if (deviceEvents.size() != 1) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_EVENT_IDENTIFIER);
        }
        eventRecord = deviceEvents.get(0);
        endDeviceEventType = (String) rawEvent.get("endDeviceEventType");
    }

    protected void getEventDevice(Map<?, ?> rawEvent) {
        long endDeviceId = getLong(rawEvent, "endDeviceId");
        Optional<Meter> meterRef = getMeteringService().findMeter(endDeviceId);
        if (!meterRef.isPresent()) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_KORE_DEVICE, endDeviceId);
        }
        Meter meter = meterRef.get();
        setKoreDevice(meter);

        Device device = getDeviceService().findDeviceById(Long.parseLong(meter.getAmrId()));
        if (device == null) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_DEVICE, meter.getAmrId());
        }
        setDevice(device);
    }

    @Override
    public String getEventType() {
        return endDeviceEventType;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue(Issue baseIssue) {
        return null;
    }

    @Override
    public void apply(Issue issue) {

    }

    @Override
    protected AbstractEvent cloneInternal() {
        MeterIssueEvent event = new MeterIssueEvent(getIssueService(), getMeteringService(), getDeviceService(), getThesaurus());
        event.endDeviceEventType = endDeviceEventType;
        event.eventRecord = eventRecord;
        return event;
    }

    @Override
    protected int getNumberOfDevicesWithEvents(Device concentrator) {
        Date start = getLastSuccessfulCommunicationEnd(concentrator);
        return concentrator.countNumberOfEndDeviceEvents(Arrays.asList(eventRecord.getEventType()), Interval.startAt(start));
    }

}