/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.entity;

import com.elster.jupiter.issue.share.entity.Issue;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import java.util.List;

public interface DeviceAlarm extends Issue {

    List<DeviceAlarmRelatedEvent> getDeviceAlarmRelatedEvents();

    Boolean isStatusCleared();

    void setClearedStatus();
}