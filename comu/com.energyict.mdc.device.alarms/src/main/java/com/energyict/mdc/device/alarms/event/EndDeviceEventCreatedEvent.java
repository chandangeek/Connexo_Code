/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.Inject;
import com.google.inject.Injector;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

public class EndDeviceEventCreatedEvent extends DeviceAlarmEvent {

    private long endDeviceId;
    private String endDeviceEventType;
    private Instant eventTimestamp;


    @Inject
    public EndDeviceEventCreatedEvent(DeviceAlarmService deviceAlarmService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, Thesaurus thesaurus, TimeService timeService, Clock clock, Injector injector) {
        super(deviceAlarmService, issueService, meteringService, deviceService, thesaurus, timeService, clock, injector);
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.endDeviceId = ((Number) jsonPayload.get(ModuleConstants.DEVICE_IDENTIFIER)).longValue();
            this.eventTimestamp = Instant.ofEpochSecond(((Number) jsonPayload.get(ModuleConstants.EVENT_TIMESTAMP)).longValue());
            this.endDeviceEventType = (String) jsonPayload.get(ModuleConstants.END_DEVICE_EVENT_TYPE);
        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    @Override
    public String getEventTypeMrid() {
        return endDeviceEventType;
    }


    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenDeviceAlarm) {
            OpenDeviceAlarm deviceAlarm = (OpenDeviceAlarm) issue;
            deviceAlarm.addRelatedAlarmEvent(endDeviceId, endDeviceEventType, eventTimestamp);


        }
    }
}
