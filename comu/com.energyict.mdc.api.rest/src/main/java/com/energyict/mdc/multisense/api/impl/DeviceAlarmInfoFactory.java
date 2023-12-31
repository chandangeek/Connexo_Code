/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.kore.api.v2.issue.DeviceShortInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.IssueAssigneeInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.IssuePriorityInfoFactory;
import com.elster.jupiter.kore.api.v2.issue.IssueReasonInfoFactory;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DeviceAlarmInfoFactory extends SelectableFieldFactory<DeviceAlarmInfo, DeviceAlarm> {

    private final DeviceShortInfoFactory deviceShortInfoFactory;
    private final DeviceAlarmStatusInfoFactory deviceAlarmStatusInfoFactory;
    private final IssueAssigneeInfoFactory issueAssigneeInfoFactory;
    private final IssuePriorityInfoFactory issuePriorityInfoFactory;
    private final IssueReasonInfoFactory issueReasonInfoFactory;

    @Inject
    public DeviceAlarmInfoFactory(DeviceShortInfoFactory deviceShortInfoFactory, DeviceAlarmStatusInfoFactory deviceAlarmStatusInfoFactory, IssueAssigneeInfoFactory issueAssigneeInfoFactory, IssuePriorityInfoFactory issuePriorityInfoFactory, IssueReasonInfoFactory issueReasonInfoFactory) {
        this.deviceShortInfoFactory = deviceShortInfoFactory;
        this.deviceAlarmStatusInfoFactory = deviceAlarmStatusInfoFactory;
        this.issueAssigneeInfoFactory = issueAssigneeInfoFactory;
        this.issuePriorityInfoFactory = issuePriorityInfoFactory;
        this.issueReasonInfoFactory = issueReasonInfoFactory;
    }

    public LinkInfo asLink(DeviceAlarm alarm, Relation relation, UriInfo uriInfo) {
        DeviceAlarmInfo info = new DeviceAlarmInfo();
        copySelectedFields(info, alarm, uriInfo, Arrays.asList("id", "version"));
        info.link = link(alarm, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends DeviceAlarm> alarms, Relation relation, UriInfo uriInfo) {
        return alarms.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }
    private Link link(DeviceAlarm alarm, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Alarm")
                .build(alarm.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceAlarmResource.class)
                .path(DeviceAlarmResource.class, "getAllOpenAlarms");
    }

    public DeviceAlarmInfo from(DeviceAlarm alarm, UriInfo uriInfo, Collection<String> fields) {
        DeviceAlarmInfo info = new DeviceAlarmInfo();
        copySelectedFields(info, alarm, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceAlarmInfo, DeviceAlarm>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceAlarmInfo, DeviceAlarm>> map = new HashMap<>();
        map.put("title", (alarmInfo, alarm, uriInfo) -> alarmInfo.title = alarm.getTitle());
        map.put("id", (alarmInfo, alarm, uriInfo) -> alarmInfo.id = alarm.getId());
        map.put("alarmId", (alarmInfo, alarm, uriInfo) -> alarmInfo.alarmId = alarm.getIssueId());
        map.put("reason", (alarmInfo, alarm, uriInfo) -> alarmInfo.reason = issueReasonInfoFactory.asInfo(alarm.getReason()));
        map.put("priority", (alarmInfo, alarm, uriInfo) -> alarmInfo.priority = issuePriorityInfoFactory.asInfo(alarm.getPriority()));
        map.put("priorityValue", (alarmInfo, alarm, uriInfo) -> alarmInfo.priorityValue = issuePriorityInfoFactory.getValue(alarm.getPriority()));
        map.put("status", (alarmInfo, alarm, uriInfo) -> alarmInfo.status = deviceAlarmStatusInfoFactory.from(alarm.getStatus(), alarm.getClearStatus().isCleared(), uriInfo, null));
        map.put("dueDate", (alarmInfo, alarm, uriInfo) -> alarmInfo.dueDate = alarm.getDueDate() != null ? alarm.getDueDate().toEpochMilli() : 0);
        map.put("workGroupAssignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.workGroupAssignee = issueAssigneeInfoFactory.asInfo("WORKGROUP", alarm.getAssignee()));
        map.put("userAssignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.userAssignee = issueAssigneeInfoFactory.asInfo("USER", alarm.getAssignee()));
        map.put("device", (alarmInfo, alarm, uriInfo) -> alarmInfo.device = deviceShortInfoFactory.from((Meter)alarm.getDevice(),uriInfo,null));
        map.put("creationDate", (alarmInfo, alarm, uriInfo) -> alarmInfo.creationDate = alarm.getCreateDateTime().toEpochMilli());
        map.put("version", (alarmInfo, alarm, uriInfo) -> alarmInfo.version = alarm.getVersion());
        return map;
    }
}
