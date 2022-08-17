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
        DEVICE_CODE("deviceCode"),
        LOGBOOK_ID("logBookId"),
        END_DEVICE_ID("endDeviceId"),
        RECORD_TIME("recordTime"),
        CREATE_TIME("createTime");

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
    private String deviceCode;
    @SuppressWarnings("unused")
    private long endDeviceId;
    @SuppressWarnings("unused")
    private long logBookId;
    @SuppressWarnings("unused")
    private Instant recordTime;
    @SuppressWarnings("unused")
    private Instant createTime;

    DeviceAlarmRelatedEventImpl init(EndDeviceEventRecord eventRecord) {
        this.eventRecord.set(eventRecord);
        this.deviceCode = eventRecord.getDeviceEventType() == null ? "*" : eventRecord.getDeviceEventType();
        this.eventTypeCode = eventRecord.getEventTypeCode();
        this.endDeviceId = eventRecord.getEndDevice().getId();
        this.logBookId = eventRecord.getLogBookId();
        this.recordTime = eventRecord.getCreatedDateTime();
        return this;
    }

    @Override
    public EndDeviceEventRecord getEventRecord() {
        return eventRecord.get();
    }

}
