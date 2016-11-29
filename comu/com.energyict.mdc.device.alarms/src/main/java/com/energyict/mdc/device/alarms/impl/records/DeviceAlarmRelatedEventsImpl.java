package com.energyict.mdc.device.alarms.impl.records;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvents;

import com.google.inject.Inject;

public class DeviceAlarmRelatedEventsImpl implements DeviceAlarmRelatedEvents {

    public enum Fields {
        AlARM("alarm"),
        EVENTRECORD("eventRecord")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }


    @IsPresent
    private Reference<EndDeviceEventRecord> eventRecord = Reference.empty();


    DeviceAlarmRelatedEventsImpl init(EndDeviceEventRecord eventRecord) {
        this.eventRecord.set(eventRecord);
        return this;
    }

    @Override
    public EndDeviceEventRecord getEventRecord() {
        return eventRecord.get();
    }

}
