package com.energyict.mdc.device.alarms.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.event.EventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.inject.Inject;
import com.google.inject.Injector;

import java.time.Instant;
import java.util.Map;

public class EndDeviceEventCreatedEvent extends DeviceAlarmEvent {

    private long endDeviceId;
    private String endDeviceEventType;
    private Instant eventTimestamp;


    @Inject
    public EndDeviceEventCreatedEvent(DeviceAlarmService deviceAlarmService, IssueService issueService, MeteringService meteringService, DeviceService deviceService, TopologyService topologyService, Thesaurus thesaurus, Injector injector) {
        super(deviceAlarmService, issueService, meteringService, deviceService, topologyService, thesaurus, injector);
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.endDeviceId = ((Number) jsonPayload.get("endDeviceId")).longValue();
            //FixMe - check why eventTimesamp is seconds
            this.eventTimestamp = Instant.ofEpochSecond(((Number) jsonPayload.get(ModuleConstants.EVENT_TIMESTAMP)).longValue());
            this.endDeviceEventType = (String) jsonPayload.get("endDeviceEventType");
        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }


    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenDeviceAlarm) {
            OpenDeviceAlarm deviceAlarm = (OpenDeviceAlarm) issue;
            deviceAlarm.addRelatedAlarmEvent(endDeviceId, endDeviceEventType, eventTimestamp);


        }
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        //TODO
        return null;
        //where("issue.id").isEqualTo().and(where().isNull());
    }
}
