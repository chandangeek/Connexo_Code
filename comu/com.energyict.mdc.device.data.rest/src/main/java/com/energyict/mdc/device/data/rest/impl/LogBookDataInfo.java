/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class LogBookDataInfo {

    public Instant eventDate;
    public EndDeviceEventTypeInfo eventType;
    public String deviceCode;
    public int eventLogId;
    public Instant readingDate;
    public String message;
    public long logBookId;

    public static List<LogBookDataInfo> from(List<EndDeviceEventRecord> endDeviceEventRecords, Thesaurus thesaurus) {
        return endDeviceEventRecords.stream().map(r -> from(r, thesaurus)).collect(Collectors.toList());
    }

    public static LogBookDataInfo from(EndDeviceEventRecord record, Thesaurus thesaurus){
        LogBookDataInfo info = new LogBookDataInfo();

        info.logBookId = record.getLogBookId();
        info.eventDate = record.getCreatedDateTime();
        info.eventType = EndDeviceEventTypeInfo.from(record.getEventType(), thesaurus);
        info.deviceCode = record.getDeviceEventType();
        info.eventLogId = record.getLogBookPosition();
        info.readingDate = record.getModTime() != null ? record.getModTime() : record.getCreateTime();
        info.message = record.getDescription();

        return info;
    }

}