
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
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
    public TopIssuesInfo getTopIssues(@Context SecurityContext securityContext, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        User currentUser = (User) securityContext.getUserPrincipal();
        List<IssueReason> issueReasons = new ArrayList<>();
        long issueTotalUserAssignedCount = 0L;
        long issueTotalWorkGroupAssignedCount = 0L;

        if (appKey != null && !appKey.isEmpty() && appKey.equalsIgnoreCase("INS")) {
            issueReasons = new ArrayList<>(getIssueService().query(IssueReason.class)
                    .select(where(ISSUE_TYPE).isEqualTo(getIssueService().findIssueType(IssueTypes.USAGEPOINT_DATA_VALIDATION.getName()).get())));
            issueTotalUserAssignedCount = getIssueService().getUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                    entry.getKey().equals(IssueTypes.USAGEPOINT_DATA_VALIDATION))
                    .mapToLong(Map.Entry::getValue).sum();
            issueTotalWorkGroupAssignedCount = getIssueService().getWorkGroupWithoutUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                    entry.getKey().equals(IssueTypes.USAGEPOINT_DATA_VALIDATION))
                    .mapToLong(Map.Entry::getValue).sum();
        } else if (appKey != null && !appKey.isEmpty() && appKey.equalsIgnoreCase("MDC")) {
            issueReasons = new ArrayList<>(getIssueService().query(IssueReason.class)
                    .select(where(ISSUE_TYPE).in(new ArrayList<IssueType>() {{
                        add(getIssueService().findIssueType(IssueTypes.DATA_COLLECTION.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.DATA_VALIDATION.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.DEVICE_LIFECYCLE.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.TASK.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.SERVICE_CALL_ISSUE.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.MANUAL.getName()).get());
                        add(getIssueService().findIssueType(IssueTypes.WEB_SERVICE.getName()).get());
                    }})));
            issueTotalUserAssignedCount = getIssueService().getUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                    isIssue(entry.getKey()))
                    .mapToLong(Map.Entry::getValue).sum();
            issueTotalWorkGroupAssignedCount = getIssueService().getWorkGroupWithoutUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                    isIssue(entry.getKey()))
                    .mapToLong(Map.Entry::getValue).sum();
        }
        List<IssueStatus> statuses = new ArrayList<>();
        Stream.of(IssueStatus.IN_PROGRESS, IssueStatus.OPEN).

                forEach(status ->

                        getIssueService().

                                findStatus(status).

                                ifPresent(statuses::add));
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
                                        and(conditionWG))), 1, 7, new Order[]{
                                                Order.descending(PRIORITYTOTAL),
                                                Order.descending(DUEDATE),
                                                Order.ascending(CREATIONDATE),
                                                Order.ascending(DEVICE),
                                                Order.ascending(USAGEPOINT),
                                                Order.ascending(ISSUEID),
                                                Order.ascending(REASON)
                });
        return new TopIssuesInfo(issues, issueTotalUserAssignedCount, issueTotalWorkGroupAssignedCount);
    }

    private boolean isIssue(IssueTypes issueType) {
        return issueType.equals(IssueTypes.DATA_COLLECTION) ||
                issueType.equals(IssueTypes.DATA_VALIDATION) ||
                issueType.equals(IssueTypes.DEVICE_LIFECYCLE) ||
                issueType.equals(IssueTypes.TASK) ||
                issueType.equals(IssueTypes.SERVICE_CALL_ISSUE) ||
                issueType.equals(IssueTypes.MANUAL) ||
                issueType.equals(IssueTypes.WEB_SERVICE);
    }

}
