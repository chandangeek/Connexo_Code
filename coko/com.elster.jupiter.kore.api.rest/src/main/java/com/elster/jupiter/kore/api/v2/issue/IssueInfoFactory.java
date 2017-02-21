/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;


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

public class IssueInfoFactory extends SelectableFieldFactory<IssueInfo, Issue> {

    private final DeviceShortInfoFactory deviceShortInfoFactory;
    private final IssueStatusInfoFactory issueStatusInfoFactory;
    private final IssueAssigneeInfoFactory issueAssigneeInfoFactory;
    private final IssueTypeInfoFactory issueTypeInfoFactory;
    private final IssuePriorityInfoFactory issuePriorityInfoFactory;
    private final IssueReasonInfoFactory issueReasonInfoFactory;

    @Inject
    public IssueInfoFactory(DeviceShortInfoFactory deviceShortInfoFactory, IssueStatusInfoFactory issueStatusInfoFactory, IssueAssigneeInfoFactory issueAssigneeInfoFactory, IssueTypeInfoFactory issueTypeInfoFactory, IssuePriorityInfoFactory issuePriorityInfoFactory, IssueReasonInfoFactory issueReasonInfoFactory) {
        this.deviceShortInfoFactory = deviceShortInfoFactory;
        this.issueStatusInfoFactory = issueStatusInfoFactory;
        this.issueAssigneeInfoFactory = issueAssigneeInfoFactory;
        this.issueTypeInfoFactory = issueTypeInfoFactory;
        this.issuePriorityInfoFactory = issuePriorityInfoFactory;
        this.issueReasonInfoFactory = issueReasonInfoFactory;
    }

    public LinkInfo asLink(Issue issue, Relation relation, UriInfo uriInfo) {
        IssueInfo info = new IssueInfo();
        copySelectedFields(info, issue, uriInfo, Arrays.asList("id", "version"));
        info.link = link(issue, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends Issue> issues, Relation relation, UriInfo uriInfo) {
        return issues.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(Issue issue, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Issue")
                .build(issue.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(IssueResource.class)
                .path(IssueResource.class, "getAllIssues");
    }

    public IssueInfo from(Issue issue, UriInfo uriInfo, Collection<String> fields) {
        IssueInfo info = new IssueInfo();
        copySelectedFields(info, issue, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<IssueInfo, Issue>> buildFieldMap() {
        Map<String, PropertyCopier<IssueInfo, Issue>> map = new HashMap<>();
        map.put("title", (issueInfo, issue, uriInfo) -> issueInfo.title = issue.getTitle());
        map.put("id", (issueInfo, issue, uriInfo) -> issueInfo.id = issue.getId());
        map.put("issueId", (issueInfo, issue, uriInfo) -> issueInfo.issueId = issue.getIssueId());
        map.put("reason", (issueInfo, issue, uriInfo) -> issueInfo.reason = issueReasonInfoFactory.asInfo(issue.getReason()));
        map.put("priority", (issueInfo, issue, uriInfo) -> issueInfo.priority = issuePriorityInfoFactory.asInfo(issue.getPriority()));
        map.put("priorityValue", (issueInfo, issue, uriInfo) -> issueInfo.priorityValue = issue.getPriority().getImpact() + issue.getPriority().getUrgency());
        map.put("status", (issueInfo, issue, uriInfo) -> issueInfo.status = issueStatusInfoFactory.from(issue.getStatus(), uriInfo, null));
        map.put("dueDate", (issueInfo, issue, uriInfo) -> issueInfo.dueDate = issue.getDueDate() != null ? issue.getDueDate().toEpochMilli() : 0);
        map.put("workGroupAssignee", (issueInfo, issue, uriInfo) -> issueInfo.workGroupAssignee = issueAssigneeInfoFactory.asInfo("WORKGROUP", issue.getAssignee()));
        map.put("userAssignee", (issueInfo, issue, uriInfo) -> issueInfo.userAssignee = issueAssigneeInfoFactory.asInfo("USER", issue.getAssignee()));
        map.put("device", (issueInfo, issue, uriInfo) -> issueInfo.device = deviceShortInfoFactory.from((Meter) issue.getDevice(), uriInfo, null));
        map.put("issueType", (issueInfo, issue, uriInfo) -> issueInfo.issueType = issueTypeInfoFactory.asInfo(issue.getReason().getIssueType()));
        map.put("creationDate", (issueInfo, issue, uriInfo) -> issueInfo.creationDate = issue.getCreateTime().toEpochMilli());
        map.put("version", (issueInfo, issue, uriInfo) -> issueInfo.version = issue.getVersion());
        return map;
    }

}
