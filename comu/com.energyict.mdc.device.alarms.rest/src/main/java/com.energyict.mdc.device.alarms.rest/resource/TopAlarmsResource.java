/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.alarms.rest.response.TopAlarmsInfo;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.util.conditions.Where.where;

@Path("/topalarms")
public class TopAlarmsResource extends BaseAlarmResource {

    public TopAlarmsResource() {

    }

    @GET
    @Transactional
    @Path("/alarms")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public TopAlarmsInfo getTopAlarms(@Context SecurityContext securityContext) {
        User currentUser = (User) securityContext.getUserPrincipal();
        IssueType alarmType = getIssueService().findIssueType(IssueTypes.DEVICE_ALARM.getName()).orElse(null);
        List<IssueReason> issueReasons = getIssueService().query(IssueReason.class)
                .select(where(ISSUE_TYPE).isEqualTo(alarmType))
                .stream()
                .collect(Collectors.toList());
        Query<OpenIssue> alarmQuery = getIssueService().query(OpenIssue.class, IssueReason.class, IssueType.class);
        Condition conditionAlarm = where("reason").in(issueReasons);
        Condition conditionUser = where("user").isEqualTo(currentUser);
        Condition conditionNullUser = where("user").isNull();
        Condition conditionWG = where("workGroup").in(currentUser.getWorkGroups());
        List<OpenIssue> alarms = alarmQuery.select(conditionAlarm.and(conditionUser.or(conditionNullUser.and(conditionWG))), 1, 5, Order.ascending("priorityTotal")
                .ascending("dueDate")
                .ascending("reason"));
        Optional<Long> alarmTotalUserAssignedCount = getIssueService().getUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                entry.getKey().equals(IssueTypes.DEVICE_ALARM))
                .findFirst().map(Map.Entry::getValue);
        Optional<Long> alarmTotalWorkGroupAssignedCount = getIssueService().getWorkGroupWithoutUserOpenIssueCount(currentUser).entrySet().stream().filter(entry ->
                entry.getKey().equals(IssueTypes.DEVICE_ALARM))
                .findFirst().map(Map.Entry::getValue);
        return new TopAlarmsInfo(alarms, alarmTotalUserAssignedCount.isPresent() ? alarmTotalUserAssignedCount.get() : 0L, alarmTotalWorkGroupAssignedCount.isPresent() ? alarmTotalWorkGroupAssignedCount.get() : 0L);
    }

}
