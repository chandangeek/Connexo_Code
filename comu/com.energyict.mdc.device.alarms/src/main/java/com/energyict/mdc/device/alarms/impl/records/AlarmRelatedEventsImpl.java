package com.energyict.mdc.device.alarms.impl.records;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.AlarmRelatedEvents;

import com.google.inject.Inject;

public class AlarmRelatedEventsImpl implements AlarmRelatedEvents{

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
    private Reference<DeviceAlarm> alarm = Reference.empty();

    @IsPresent
    private Reference<EndDeviceEventRecord> eventRecord = Reference.empty();

    private final DataModel dataModel;

    @Inject
    AlarmRelatedEventsImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private AlarmRelatedEventsImpl init(DeviceAlarm alarm, EndDeviceEventRecord eventRecord) {
        this.alarm.set(alarm);
        this.eventRecord.set(eventRecord);
        return this;
    }

    static AlarmRelatedEventsImpl from(DataModel dataModel,
                                       DeviceAlarm alarm, EndDeviceEventRecord eventRecord) {
        return dataModel.getInstance(AlarmRelatedEventsImpl.class).init(alarm, eventRecord);
    }

    @Override
    public EndDeviceEventRecord getEvent() {
        return eventRecord.get();
    }

    public DataModel getDataModel() {
        return dataModel;
    }
}
