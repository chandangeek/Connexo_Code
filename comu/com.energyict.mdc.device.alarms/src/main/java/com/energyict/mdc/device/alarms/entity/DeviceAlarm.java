package com.energyict.mdc.device.alarms.entity;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceAlarm extends Issue {

    List<DeviceAlarmRelatedEvent> getDeviceAlarmRelatedEvents();

    List<EndDeviceEvent> getEndDeviceRelatedEvents();

    EndDeviceEvent getCurrentEvent();

    Boolean getClearedStatus();

    void setClearedStatus();
}