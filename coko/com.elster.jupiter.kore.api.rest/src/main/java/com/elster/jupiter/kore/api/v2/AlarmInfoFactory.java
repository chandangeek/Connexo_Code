package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

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

public class AlarmInfoFactory extends SelectableFieldFactory<AlarmInfo, Issue> {

    private final EndDeviceInfoFactory endDeviceInfoFactory;
    private final IssueStatusInfoFactory issueStatusInfoFactory;
    private final IssueAssigneeInfoFactory issueAssigneeInfoFactory;
    private final IssueTypeInfoFactory issueTypeInfoFactory;
    private final IssuePriorityInfoFactory issuePriorityInfoFactory;
    private final IssueReasonInfoFactory issueReasonInfoFactory;

    @Inject
    public AlarmInfoFactory(EndDeviceInfoFactory endDeviceInfoFactory, IssueStatusInfoFactory issueStatusInfoFactory, IssueAssigneeInfoFactory issueAssigneeInfoFactory, IssueTypeInfoFactory issueTypeInfoFactory, IssuePriorityInfoFactory issuePriorityInfoFactory, IssueReasonInfoFactory issueReasonInfoFactory) {
        this.endDeviceInfoFactory = endDeviceInfoFactory;
        this.issueStatusInfoFactory = issueStatusInfoFactory;
        this.issueAssigneeInfoFactory = issueAssigneeInfoFactory;
        this.issueTypeInfoFactory = issueTypeInfoFactory;
        this.issuePriorityInfoFactory = issuePriorityInfoFactory;
        this.issueReasonInfoFactory = issueReasonInfoFactory;
    }

    public LinkInfo asLink(Issue alarm, Relation relation, UriInfo uriInfo) {
        AlarmInfo info = new AlarmInfo();
        copySelectedFields(info, alarm, uriInfo, Arrays.asList("id", "version"));
        info.link = link(alarm, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends Issue> alarms, Relation relation, UriInfo uriInfo) {
        return alarms.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }
    private Link link(Issue alarm, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Alarm")
                .build(alarm.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(AlarmResource.class)
                .path(AlarmResource.class, "getAllAlarms");
    }

    public AlarmInfo from(Issue alarm, UriInfo uriInfo, Collection<String> fields) {
        AlarmInfo info = new AlarmInfo();
        copySelectedFields(info, alarm, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<AlarmInfo, Issue>> buildFieldMap() {
        Map<String, PropertyCopier<AlarmInfo, Issue>> map = new HashMap<>();
        map.put("id", (alarmInfo, alarm, uriInfo) -> alarmInfo.id = alarm.getId());
        map.put("alarmId", (alarmInfo, alarm, uriInfo) -> alarmInfo.alarmId = alarm.getIssueId());
        map.put("reason", (alarmInfo, alarm, uriInfo) -> alarmInfo.reason = issueReasonInfoFactory.asInfo(alarm.getReason()));
        map.put("priority", (alarmInfo, alarm, uriInfo) -> alarmInfo.priority = issuePriorityInfoFactory.asInfo(alarm.getPriority()));
        map.put("priorityValue", (alarmInfo, alarm, uriInfo) -> alarmInfo.priorityValue = issuePriorityInfoFactory.getValue(alarm.getPriority()));
        map.put("status", (alarmInfo, alarm, uriInfo) -> alarmInfo.status = issueStatusInfoFactory.from(alarm.getStatus(), uriInfo, null));
        map.put("dueDate", (alarmInfo, alarm, uriInfo) -> alarmInfo.dueDate = alarm.getDueDate() != null ? alarm.getDueDate().toEpochMilli() : 0);
        map.put("assignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.assignee = issueAssigneeInfoFactory.asInfo(alarm.getAssignee()));
        map.put("workGroupAssignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.workGroupAssignee = issueAssigneeInfoFactory.asInfo("WORKGROUP", alarm.getAssignee()));
        map.put("userAssignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.userAssignee = issueAssigneeInfoFactory.asInfo("USER", alarm.getAssignee()));
        map.put("device", (alarmInfo, alarm, uriInfo) -> alarmInfo.device = endDeviceInfoFactory.from((Meter)alarm.getDevice(),uriInfo,null));
        map.put("assignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.title = alarm.getTitle());
        map.put("assignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.alarmType = issueTypeInfoFactory.asInfo(alarm.getReason().getIssueType()));
        map.put("assignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.creationDate = alarm.getCreateTime().toEpochMilli());
        map.put("assignee", (alarmInfo, alarm, uriInfo) -> alarmInfo.modTime = alarm.getModTime().toEpochMilli());
        map.put("version", (alarmInfo, alarm, uriInfo) -> alarmInfo.version = alarm.getVersion());
        return map;
    }
}
