package com.energyict.mdc.device.data.rest.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;

public class LogBookDataInfo {
    
    public Date eventDate;
    public EndDeviceEventTypeInfo eventType;
    public String deviceCode;
    public int eventLogId;
    public Date readingDate;
    public String message;
    
    public static List<LogBookDataInfo> from(List<EndDeviceEventRecord> endDeviceEventRecords, Thesaurus thesaurus) {
        List<LogBookDataInfo> infos = new ArrayList<>(endDeviceEventRecords.size());
        for(EndDeviceEventRecord record : endDeviceEventRecords) {
            LogBookDataInfo info = new LogBookDataInfo();
            
            info.eventDate = record.getCreatedDateTime();
            info.eventType = EndDeviceEventTypeInfo.from(record.getEventType(), thesaurus);
            info.deviceCode = record.getDeviceEventType();
            info.eventLogId = record.getLogBookPosition();
            info.readingDate = record.getModTime() != null ? record.getModTime() : record.getCreateTime();
            info.message = record.getDescription();

            infos.add(info);
        }
        
        return infos;
    }
}
