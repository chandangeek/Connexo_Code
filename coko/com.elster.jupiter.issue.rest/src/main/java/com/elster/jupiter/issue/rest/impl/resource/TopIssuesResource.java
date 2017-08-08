/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.TopIssuesInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.issue.rest.request.RequestHelper.DUEDATE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.PRIORITYTOTAL;
import static com.elster.jupiter.issue.rest.request.RequestHelper.REASON;
import static com.elster.jupiter.issue.rest.request.RequestHelper.STATUS;
import static com.elster.jupiter.issue.rest.request.RequestHelper.USER;
import static com.elster.jupiter.issue.rest.request.RequestHelper.WORKGROUP;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/topissues")
public class TopIssuesResource extends BaseResource {

    public TopIssuesResource() {

    }

    @GET
    @Transactional
    @Path("/issues")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public TopIssuesInfo getTopIssues(@Context SecurityContext securityContext) {
        User currentUser = (User) securityContext.getUserPrincipal();
        List<IssueType> issueTypes = new ArrayList<>();
        Stream.of(IssueTypes.DATA_COLLECTION.getName(), IssueTypes.DATA_VALIDATION.getName())
                .forEach(issueType -> getIssueService().findIssueType(issueType).ifPresent(issueTypes::add));
        List<IssueReason> issueReasons = getIssueService().query(IssueReason.class)
                .select(where(ISSUE_TYPE).in(issueTypes))
                .stream()
                .collect(Collectors.toList());
        List<IssueStatus> statuses = new ArrayList<>();
        Stream.of(IssueStatus.IN_PROGRESS, IssueStatus.OPEN).forEach(status -> getIssueService().findStatus(status).ifPresent(statuses::add));
        Query<OpenIssue> issueQuery =
                getIssueService().query(OpenIssue.class, IssueReason.class, IssueType.class);
        Condition conditionReason = where(REASON).in(issueReasons);
        Condition conditionStatus = where(STATUS).in(statuses);
        Condition conditionUser = where(USER).isEqualTo(currentUser);
        Condition conditionNullUser = where(USER).isNull();
        Condition conditionWG = where(WORKGROUP).in(currentUser.getWorkGroups());
        List<OpenIssue> issues = issueQuery.select(
                conditionReason.
                        and(conditionStatus).
                        and(conditionUser.
                                or(conditionNullUser.
                                        and(conditionWG))), 1, 5, Order.ascending(PRIORITYTOTAL)
                        .ascending(DUEDATE)
                        .ascending(REASON));
        long issueTotalUserAssignedCount = getIssueService().getUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                entry.getKey().equals(IssueTypes.DATA_COLLECTION) || entry.getKey().equals(IssueTypes.DATA_VALIDATION))
                .mapToLong(Map.Entry::getValue).sum();
        long issueTotalWorkGroupAssignedCount = getIssueService().getWorkGroupWithoutUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                entry.getKey().equals(IssueTypes.DATA_COLLECTION) || entry.getKey().equals(IssueTypes.DATA_VALIDATION))
                .mapToLong(Map.Entry::getValue).sum();

        return new TopIssuesInfo(issues, issueTotalUserAssignedCount, issueTotalWorkGroupAssignedCount);
    }

}
