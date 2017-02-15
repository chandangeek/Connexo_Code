/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.energyict.mdc.device.alarms.rest.response.TopAlarmsInfo;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/topalarms")
public class TopAlarmsResource extends BaseAlarmResource{

    public TopAlarmsResource(){

    }

    @GET
    @Transactional
    @Path("/alarms")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public TopAlarmsInfo getTopAlarms(@Context SecurityContext securityContext) {
        User currentUser = (User) securityContext.getUserPrincipal();
        Comparator<Issue> dueDateComparator = Comparator.comparing(Issue::getDueDate, Comparator.nullsLast(Instant::compareTo));
        Comparator<Issue> priorityComparator = (alarm1, alarm2) -> Integer.compare(alarm2.getPriority().getImpact() + alarm2.getPriority().getUrgency(), alarm1.getPriority().getImpact() + alarm1.getPriority().getUrgency());
        Comparator<Issue> nameComparator = (alarm1, alarm2) -> alarm1.getTitle().toLowerCase().compareTo(alarm2.getTitle());
        Finder<? extends Issue> finder = getIssueService().findAlarms();
        List<? extends Issue> alarms = finder.find();
        List<Issue> items = getItems(alarms, currentUser);
        return new TopAlarmsInfo(items.stream()
                .sorted(priorityComparator.thenComparing(dueDateComparator).thenComparing(nameComparator))
                .limit(5)
                .collect(Collectors.toList()), getTotalUserAssigned(items, currentUser), getTotalWorkGroupAssigned(items, currentUser));
    }

    private List<Issue> getItems(List<? extends Issue> issues, User currentUser){
        List<Issue> items = new ArrayList<>();
        for (Issue baseIssue : issues) {
            for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
                Optional<? extends Issue> issueRef = issueProvider.findIssue(baseIssue.getId());
                if (issueRef.isPresent()) {
                    items.add(issueRef.get());
                }
            }
        }
        return items.stream()
                .filter(item -> (item.getAssignee().getUser() != null && item.getAssignee().getUser().equals(currentUser)) ||
                        (item.getAssignee().getWorkGroup() != null && currentUser.getWorkGroups()
                                .stream()
                                .map(WorkGroup::getName)
                                .anyMatch(workGroupName -> workGroupName.equals(item.getAssignee().getWorkGroup().getName()))
                                && item.getAssignee().getUser() == null))
                .collect(Collectors.toList());
    }

    private long getTotalUserAssigned(List<Issue> items, User currentUser){
        return items.stream()
                .filter(item ->item.getAssignee().getUser() != null && item.getAssignee().getUser().equals(currentUser))
                .count();
    }

    private long getTotalWorkGroupAssigned(List<Issue> items, User currentUser){
        return items.stream()
                .filter(item -> item.getAssignee().getWorkGroup() != null && currentUser.getWorkGroups()
                        .stream()
                        .map(WorkGroup::getName)
                        .anyMatch(workGroupName -> workGroupName.equals(item.getAssignee().getWorkGroup().getName()))
                        && item.getAssignee().getUser() == null)
                .count();
    }

}
