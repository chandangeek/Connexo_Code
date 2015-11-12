package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionFilter;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/myopenissuesoverview")
public class IssuesResource {
    private static final String UNEXISTING_TYPE = "UnexistingType";
    private static final int TOP_MY_ISSUES_LIMIT = 5;
    private IssueDataCollectionService issueDataCollectionService;
    private IssueService issueService;

    @Inject
    public IssuesResource(IssueDataCollectionService issueDataCollectionService, IssueService issueService) {
        this.issueDataCollectionService = issueDataCollectionService;
        this.issueService = issueService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getIssues(@Context SecurityContext context) {
        User user = (User) context.getUserPrincipal();
        MyOpenIssuesInfo myOpenIssuesInfo = new MyOpenIssuesInfo();
        List<IssueStatus> statuses = Arrays.asList(issueService.findStatus(IssueStatus.OPEN).get(), issueService.findStatus(IssueStatus.IN_PROGRESS).get());

        appendUnassignedIssues(myOpenIssuesInfo, statuses);
        appendMyIssues(myOpenIssuesInfo, statuses, user);
        return Response.ok(myOpenIssuesInfo).build();
    }

    // Get unassigned issues
    private void appendUnassignedIssues(MyOpenIssuesInfo response, List<IssueStatus> statuses) {
        IssueDataCollectionFilter filter = new IssueDataCollectionFilter();
        statuses.stream().forEach(filter::addStatus);
        filter.setUnassignedOnly();
        List<? extends IssueDataCollection> issues = issueDataCollectionService.findIssues(filter).find();

        response.unassignedIssues = new IssuesCollectionInfo();
        response.unassignedIssues.filter = new IssuesCollectionFilterInfo();

        response.unassignedIssues.total = issues.size();
        response.unassignedIssues.filter.assigneeType = UNEXISTING_TYPE;
        response.unassignedIssues.filter.assigneeId = -1L;
        response.unassignedIssues.topMyIssues = null;
    }

    // Get assigned to me issues
    private void appendMyIssues(MyOpenIssuesInfo myOpenIssuesInfo, List<IssueStatus> statuses, User user) {
        IssueDataCollectionFilter filter = new IssueDataCollectionFilter();
        statuses.stream().forEach(filter::addStatus);
        filter.setAssignee(user);
        List<? extends IssueDataCollection> issues = issueDataCollectionService.findIssues(filter).sorted("baseIssue.dueDate", true).find();

        myOpenIssuesInfo.assignedToMeIssues = new IssuesCollectionInfo();
        myOpenIssuesInfo.assignedToMeIssues.filter = new IssuesCollectionFilterInfo();
        myOpenIssuesInfo.assignedToMeIssues.topMyIssues = new ArrayList<>();

        myOpenIssuesInfo.assignedToMeIssues.total = issues.size();
        myOpenIssuesInfo.assignedToMeIssues.filter.assigneeType = IssueAssignee.Types.USER;
        myOpenIssuesInfo.assignedToMeIssues.filter.assigneeId = user.getId();

        for (int i = 0; i < TOP_MY_ISSUES_LIMIT && i < issues.size(); i++) {
            IssueDataCollection issue = issues.get(i);
            IssueInfo issueInfo = new IssueInfo();
            issueInfo.id = issue.getId();
            issueInfo.title = issue.getTitle();
            issueInfo.dueDate = issue.getDueDate() != null ? issue.getDueDate().toEpochMilli() : null;
            myOpenIssuesInfo.assignedToMeIssues.topMyIssues.add(issueInfo);
        }
    }
}