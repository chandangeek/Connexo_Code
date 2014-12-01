package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

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
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({com.elster.jupiter.issue.security.Privileges.VIEW_ISSUE, com.elster.jupiter.issue.security.Privileges.ASSIGN_ISSUE, com.elster.jupiter.issue.security.Privileges.CLOSE_ISSUE, com.elster.jupiter.issue.security.Privileges.COMMENT_ISSUE, com.elster.jupiter.issue.security.Privileges.ACTION_ISSUE})
    public Response getIssues(@Context SecurityContext context) {
        User user = (User) context.getUserPrincipal();
        Query<OpenIssueDataCollection> query;
        Condition condition;
        List<OpenIssueDataCollection> issues;
        MyOpenIssuesInfo myOpenIssuesInfo = new MyOpenIssuesInfo();
        IssueStatus openStatus = issueService.findStatus(IssueStatus.OPEN).get();

        // Get unassigned issues
        query = issueDataCollectionService.query(OpenIssueDataCollection.class, OpenIssue.class);
        condition = where("baseIssue.assigneeType").isNull().and(where("baseIssue.status").isEqualTo(openStatus));
        issues = query.select(condition);

        myOpenIssuesInfo.unassignedIssues = new IssuesCollectionInfo();
        myOpenIssuesInfo.unassignedIssues.filter = new IssuesCollectionFilterInfo();

        myOpenIssuesInfo.unassignedIssues.total = issues.size();
        myOpenIssuesInfo.unassignedIssues.filter.assigneeType = UNEXISTING_TYPE;
        myOpenIssuesInfo.unassignedIssues.filter.assigneeId = -1L;
        myOpenIssuesInfo.unassignedIssues.topMyIssues = null;


        // Get assigned to me issues
        query = issueDataCollectionService.query(OpenIssueDataCollection.class, OpenIssue.class);
        condition = Condition.TRUE.and(where("baseIssue.user").isEqualTo(user)).and(where("baseIssue.status").isEqualTo(openStatus));
        issues = query.select(condition, Order.ascending("baseIssue.dueDate"));

        myOpenIssuesInfo.assignedToMeIssues = new IssuesCollectionInfo();
        myOpenIssuesInfo.assignedToMeIssues.filter = new IssuesCollectionFilterInfo();
        myOpenIssuesInfo.assignedToMeIssues.topMyIssues = new ArrayList<>();

        myOpenIssuesInfo.assignedToMeIssues.total = issues.size();
        myOpenIssuesInfo.assignedToMeIssues.filter.assigneeType = IssueAssignee.Types.USER;
        myOpenIssuesInfo.assignedToMeIssues.filter.assigneeId = user.getId();

        for (int i = 0; i < TOP_MY_ISSUES_LIMIT && i < issues.size(); i++) {
            OpenIssueDataCollection issue = issues.get(i);
            IssueInfo issueInfo = new IssueInfo();
            issueInfo.id = issue.getId();
            issueInfo.title = issue.getTitle();
            issueInfo.dueDate = issue.getDueDate() != null ? issue.getDueDate().toEpochMilli() : null;
            myOpenIssuesInfo.assignedToMeIssues.topMyIssues.add(issueInfo);
        }

        return Response.ok(myOpenIssuesInfo).build();
    }
}