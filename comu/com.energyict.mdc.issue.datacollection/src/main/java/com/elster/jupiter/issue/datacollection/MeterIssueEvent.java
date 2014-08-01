package com.elster.jupiter.issue.datacollection;

import com.elster.jupiter.issue.datacollection.impl.AbstractEvent;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.UnableToCreateEventException;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.google.common.base.Optional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MeterIssueEvent extends AbstractEvent {
    private String endDeviceEventType;
    private EndDeviceEventRecord eventRecord;

    protected MeterIssueEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, Thesaurus thesaurus) {
        super(issueService, meteringService, deviceDataService, thesaurus);
    }

    public MeterIssueEvent(IssueService issueService, MeteringService meteringService, DeviceDataService deviceDataService, Thesaurus thesaurus, Map<?, ?> rawEvent) {
        super(issueService, meteringService, deviceDataService, thesaurus, rawEvent);
    }

    @Override
    protected void init(Map<?, ?> rawEvent) {
        super.init(rawEvent);

        long timestamp = getLong(rawEvent, ModuleConstants.EVENT_TIMESTAMP);
        List<EndDeviceEventRecord> deviceEvents = getDevice().getDeviceEvents(new Interval(new Date(timestamp), new Date(timestamp)));
        if (deviceEvents.size() != 1){
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_EVENT_IDENTIFIER);
        }
        eventRecord = deviceEvents.get(0);
        endDeviceEventType = (String) rawEvent.get("endDeviceEventType");
    }

    protected void getEventDevice(Map<?, ?> rawEvent){
        long endDeviceId = getLong(rawEvent,"endDeviceId");
        Optional<Meter> meterRef = getMeteringService().findMeter(endDeviceId);
        if (!meterRef.isPresent()){
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.EVENT_BAD_DATA_NO_END_DEVICE, endDeviceId);
        }
        Meter meter = meterRef.get();
        setEndDevice(meter);

        Device device = getDeviceDataService().findDeviceById(Long.valueOf(meter.getAmrId()));
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
    protected AbstractEvent cloneInternal() {
        MeterIssueEvent event = new MeterIssueEvent(getIssueService(), getMeteringService(), getDeviceDataService(), getThesaurus());
        event.endDeviceEventType = endDeviceEventType;
        event.eventRecord = eventRecord;
        return event;
    }

    @Override
    protected int getNumberOfEvents(Device concentrator) {
        Date start = getLastSuccessfulCommunicationEnd(concentrator);
        return concentrator.countNumberOfEndDeviceEvents(Arrays.asList(eventRecord.getEventType()), Interval.startAt(start));
    }
}
