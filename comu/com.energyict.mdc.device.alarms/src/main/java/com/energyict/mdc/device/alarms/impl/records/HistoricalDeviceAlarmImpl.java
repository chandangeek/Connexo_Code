package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

public class HistoricalDeviceAlarmImpl extends DeviceAlarmImpl implements HistoricalDeviceAlarm {

    @IsPresent
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();

    @Inject
    public HistoricalDeviceAlarmImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Valid
    private List<HistoricalDeviceAlarmRelatedEventImpl> deviceAlarmRelatedEvents;

    protected HistoricalIssue getBaseIssue() {
        return baseIssue.orNull();
    }

    void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenDeviceAlarm alarm) {
        this.setId(alarm.getId());
        for (DeviceAlarmRelatedEvent event : alarm.getDeviceAlarmRelatedEvents()) {
            HistoricalDeviceAlarmRelatedEventImpl alarmEvent = getDataModel().getInstance(HistoricalDeviceAlarmRelatedEventImpl.class);
            alarmEvent.init(this, event.getEventRecord());
            deviceAlarmRelatedEvents.add(alarmEvent);
        }
    }
}
