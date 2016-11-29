package com.energyict.mdc.device.alarms.impl.records;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvents;

public class DeviceAlarmRelatedEventsImpl implements DeviceAlarmRelatedEvents {

    public enum Fields {
        AlARM("alarm"),
        EVENT("event")
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
    private Reference<EndDeviceEvent> event = Reference.empty();


    DeviceAlarmRelatedEventsImpl init(EndDeviceEvent event) {
        this.event.set(event);
        return this;
    }

    @Override
    public EndDeviceEvent getEvent() {
        return event.get();
    }

}
