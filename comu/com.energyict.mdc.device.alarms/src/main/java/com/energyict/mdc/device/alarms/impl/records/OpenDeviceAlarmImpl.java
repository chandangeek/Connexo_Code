package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class OpenDeviceAlarmImpl extends DeviceAlarmImpl implements OpenDeviceAlarm {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Valid
    private List<OpenDeviceAlarmRelatedEventImpl> deviceAlarmRelatedEvents = new ArrayList<>();

    @Inject
    public OpenDeviceAlarmImpl(DataModel dataModel, DeviceAlarmService deviceAlarmService) {
        super(dataModel, deviceAlarmService);
    }

    public OpenIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseAlarm) {
        this.baseIssue.set(baseAlarm);
    }

    public HistoricalDeviceAlarm close(IssueStatus status) {
        HistoricalDeviceAlarmImpl historicalDeviceAlarm = getDataModel().getInstance(HistoricalDeviceAlarmImpl.class);
        historicalDeviceAlarm.copy(this);
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseAlarm = getBaseIssue().closeInternal(status);
        historicalDeviceAlarm.setIssue(historicalBaseAlarm);
        historicalDeviceAlarm.save();
        return historicalDeviceAlarm;
    }


    @Override
    public List<DeviceAlarmRelatedEvent> getDeviceAlarmRelatedEvents(){
        return Collections.unmodifiableList(deviceAlarmRelatedEvents);
    }

    @Override
    public void addRelatedAlarmEvent(long endDeviceId, String endDeviceEventType, Instant eventTimestamp) {
        createNewRelatedAlarmEvent(endDeviceId, endDeviceEventType, eventTimestamp);
    }

    @Override
    public void removeRelatedAlarmEvent(EndDeviceEvent event, Instant timeStamp) {

    }


    private void createNewRelatedAlarmEvent(long endDeviceId, String endDeviceEventType, Instant eventTimestamp) {
        OpenDeviceAlarmRelatedEventImpl event = getDataModel().getInstance(OpenDeviceAlarmRelatedEventImpl.class);

        MeteringService meteringService = getDataModel().getInstance(MeteringService.class);
        Optional<EndDeviceEventType> eventType = meteringService.getEndDeviceEventType(endDeviceEventType);
        if (getDevice().getId() == endDeviceId && eventType.isPresent()) {
            //FixMe - endDeviceEventType not avaliable in system
            /*if (!eventType.isPresent()) {
                eventType = Optional.of(meteringService.createEndDeviceEventType(endDeviceEventType));
            }*/
            List<EndDeviceEventRecord> events = getDevice().getDeviceEvents(Range.closedOpen(eventTimestamp, eventTimestamp.plusMillis(1)), Collections.singletonList(eventType
                    .get()));
            // Beautify
            if (events.size() == 1) {
                event.init(this, events.get(0));
                deviceAlarmRelatedEvents.add(event);
            }
        }

    }
}
