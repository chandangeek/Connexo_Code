/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;


import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;


public class RelatedEventsInfo {

    public String deviceCode;
    public String deviceType;
    public String domain;
    public String subDomain;
    public String eventOrAction;
    public long eventDate;

    public RelatedEventsInfo(EndDeviceEventRecord endDeviceEventRecord){
        EndDeviceEventType endDeviceEventType = endDeviceEventRecord.getEventType();
        this.deviceCode = endDeviceEventRecord.getDeviceEventType();
        this.deviceType = endDeviceEventType.getType().getMnemonic() + " (" + endDeviceEventType.getType().getCode() + ")";
        this.domain = endDeviceEventType.getDomain().getMnemonic() + " (" + endDeviceEventType.getDomain().getCode() + ")";
        this.subDomain = endDeviceEventType.getSubDomain().getMnemonic() + " (" + endDeviceEventType.getSubDomain().getCode() + ")";
        this.eventOrAction = endDeviceEventType.getEventOrAction().getMnemonic() + " (" + endDeviceEventType.getEventOrAction().getCode() + ")";
        this.eventDate = endDeviceEventRecord.getCreatedDateTime().toEpochMilli();
    }

}
