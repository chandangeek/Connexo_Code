package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class CloseIssuesTransaction implements Transaction<ActionInfo> {
    private final IssueService issueService;
    private final CloseIssueRequest request;
    private final User author;


    public CloseIssuesTransaction(CloseIssueRequest request, IssueService issueService, User author) {
        this.request = request;
        this.issueService = issueService;
        this.author = author;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        IssueStatus status = issueService.findStatus(request.getStatus()).orNull();
        if (request.getIssues() != null && status != null && status.isFinal()) {
            for (EntityReference issueRef : request.getIssues()) {
                Issue issue = issueService.findIssue(issueRef.getId()).orNull();
                if (issue == null) {
                    response.addFail("Issue doesn't exist", issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
                } else if (issueRef.getVersion() != issue.getVersion()){
                    response.addFail("Issue has been already changed", issueRef.getId(), issue.getTitle());
                } else {
                    issue.addComment(request.getComment(), author);
                    issue.close(status);
                    response.addSuccess(issueRef.getId());
                }
            }
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return response;
    }
}
