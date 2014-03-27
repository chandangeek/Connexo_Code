package com.elster.jupiter.issue.rest.transactions;


import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssigneeType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class AssignIssueTransaction  implements Transaction<ActionInfo> {
    private final IssueService issueService;
    private final AssignIssueRequest request;
    private final User author;

    public AssignIssueTransaction(AssignIssueRequest request, IssueService issueService, User author){
        this.request = request;
        this.issueService = issueService;
        this.author = author;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        IssueAssigneeType newAssigneeType = getAssigneeForIssue();
        if (request.getIssues() != null && newAssigneeType != null) {
            for (EntityReference issueRef : request.getIssues()) {
                Issue issue = issueService.findIssue(issueRef.getId()).orNull();
                if (issue == null) {
                    response.addFail("Issue doesn't exist", issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
                } else if (issueRef.getVersion() != issue.getVersion()){
                    response.addFail("Issue has been already changed", issueRef.getId(), issue.getTitle());
                } else {
                    issue.assignTo(newAssigneeType, request.getAssignee().getId());
                    issue.addComment(request.getComment(), author);
                    issue.update();
                    response.addSuccess(issueRef.getId());
                }
            }
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return response;
    }

    private IssueAssigneeType getAssigneeForIssue(){
        if (request.getAssignee() != null){
            return IssueAssigneeType.fromString(request.getAssignee().getType());
        }
        return null;
    }
}
