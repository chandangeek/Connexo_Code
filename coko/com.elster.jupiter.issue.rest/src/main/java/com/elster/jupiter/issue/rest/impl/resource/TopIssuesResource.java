package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.response.TopIssuesInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

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

@Path("/topissues")
public class TopIssuesResource extends BaseResource {

    public TopIssuesResource(){

    }

    @GET
    @Transactional
    @Path("/issues")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public TopIssuesInfo getTopIssues(@Context SecurityContext securityContext) {
        User currentUser = (User) securityContext.getUserPrincipal();
        Comparator<Issue> dueDateComparator = Comparator.comparing(Issue::getDueDate, Comparator.nullsLast(Instant::compareTo));
        Comparator<Issue> priorityComparator = (issue1, issue2) -> Integer.compare(issue1.getPriority().getImpact() + issue1.getPriority().getUrgency(), issue2.getPriority().getImpact() + issue2.getPriority().getUrgency());
        Comparator<Issue> nameComparator = (issue1, issue2) -> issue1.getTitle().toLowerCase().compareTo(issue2.getTitle());
        Finder<? extends Issue> finder = getIssueService().findIssues(getIssueService().newIssueFilter());
        List<? extends Issue> issues = finder.find();
        List<Issue> items = getItems(issues, currentUser);
        return new TopIssuesInfo(items.stream()
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
                    if(!issueRef.get().getStatus().isHistorical()) {
                        items.add(issueRef.get());
                    }
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
