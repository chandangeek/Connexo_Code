package com.energyict.mdc.device.alarms.entity;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceAlarm extends Issue {

    String getDeviceMRID();

    void setDeviceMRID(String deviceMRID);

    List<EndDeviceEvent> getRelatedEvent();

    EndDeviceEvent getCurrentEvent();

    Boolean getClearedStatus();

    void setClearedStatus();
}