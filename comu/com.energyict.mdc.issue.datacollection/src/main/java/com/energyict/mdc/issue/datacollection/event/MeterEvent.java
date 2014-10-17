package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.UnableToCreateEventException;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.inject.Injector;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MeterEvent extends DataCollectionEvent {
    private String endDeviceEventType;
    private EndDeviceEventRecord eventRecord;

    @Inject
    public MeterEvent(IssueDataCollectionService issueDataCollectionService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, CommunicationTaskService communicationTaskService, Thesaurus thesaurus, Injector injector) {
        super(issueDataCollectionService, issueService, meteringService, deviceService, communicationTaskService, thesaurus, injector);
    }

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription) {
        setEventDescription(eventDescription);
        setDefaultIssueStatus();
        wrapInternal(rawEvent, eventDescription);
    }

    @Override
    protected void wrapInternal(Map<?, ?> rawEvent, EventDescription eventDescription) {
        long timestamp = getLong(rawEvent, ModuleConstants.EVENT_TIMESTAMP);
        List<EndDeviceEventRecord> deviceEvents =
                getKoreDevice().getDeviceEvents(
                        Range.range(
                                Instant.ofEpochMilli(timestamp), BoundType.OPEN,
                                Instant.ofEpochMilli(timestamp), BoundType.CLOSED));
        if (deviceEvents.size() != 1) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_EVENT_IDENTIFIER);
        }
        eventRecord = deviceEvents.get(0);
        endDeviceEventType = (String) rawEvent.get("endDeviceEventType");
    }

    @Override
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
    protected Condition getConditionForExistingIssue() {
        return Condition.FALSE; // TODO which fields are specific for this event?
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenIssueDataCollection) {
            // TODO which fields are specific for this event?
        }
    }

    @Override
    protected int getNumberOfDevicesWithEvents(Device concentrator) {
        Instant start = getLastSuccessfulCommunicationEnd(concentrator);
        return concentrator.countNumberOfEndDeviceEvents(Arrays.asList(eventRecord.getEventType()), Interval.startAt(start));
    }

    @Override
    public DataCollectionEvent clone() {
        MeterEvent clone = (MeterEvent) super.clone();
        clone.eventRecord = eventRecord;
        clone.endDeviceEventType = endDeviceEventType;
        return clone;
    }

}