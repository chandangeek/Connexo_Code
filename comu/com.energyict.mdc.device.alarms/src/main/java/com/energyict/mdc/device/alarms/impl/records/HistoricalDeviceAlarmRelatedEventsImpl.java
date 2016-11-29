package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.HistoricalDeviceAlarmRelatedEvents;

public class HistoricalDeviceAlarmRelatedEventsImpl extends DeviceAlarmRelatedEventsImpl implements HistoricalDeviceAlarmRelatedEvents{

    @IsPresent
    private Reference<DeviceAlarm> alarm = Reference.empty();


    DeviceAlarmRelatedEventsImpl init(DeviceAlarm alarm, EndDeviceEventRecord eventRecord){
        this.alarm.set(alarm);
        super.init(eventRecord);
        return this;
    }

}
