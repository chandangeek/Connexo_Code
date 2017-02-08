/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.records;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import java.time.Instant;

public class DeviceAlarmRelatedEventImpl implements DeviceAlarmRelatedEvent {

    public enum Fields {
        AlARM("alarm"),
        EVENTRECORD("eventRecord"),
        EVENT_TYPE_CODE("eventTypeCode"),
        END_DEVICE_ID("endDeviceId"),
        CREATE_DATE_TIME("createdDateTime");

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
    @SuppressWarnings("unused")
    private String eventTypeCode;
    @SuppressWarnings("unused")
    private long endDeviceId;
    @SuppressWarnings("unused")
    private Instant createdDateTime;

    DeviceAlarmRelatedEventImpl init(EndDeviceEventRecord eventRecord) {
        this.eventRecord.set(eventRecord);
        this.eventTypeCode = eventRecord.getEventTypeCode();
        this.endDeviceId = eventRecord.getEndDevice().getId();
        this.createdDateTime = eventRecord.getCreatedDateTime();
        return this;
    }

    @Override
    public EndDeviceEventRecord getEventRecord() {
        return eventRecord.get();
    }

}
