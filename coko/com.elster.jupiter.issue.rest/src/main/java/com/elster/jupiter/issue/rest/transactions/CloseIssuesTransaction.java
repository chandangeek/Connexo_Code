package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.*;

public class CloseIssuesTransaction implements Transaction<ActionInfo> {
    private final IssueService issueService;
    private final CloseIssueRequest request;
    private final User author;
    private final Thesaurus thesaurus;

    public CloseIssuesTransaction(CloseIssueRequest request, IssueService issueService, User author, Thesaurus thesaurus) {
        this.request = request;
        this.issueService = issueService;
        this.author = author;
        this.thesaurus = thesaurus;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        IssueStatus status = issueService.findStatus(request.getStatus()).orNull();
        if (request.getIssues() != null && status != null && status.isFinal()) {
            for (EntityReference issueRef : request.getIssues()) {
                Issue issue = issueService.findIssue(issueRef.getId()).orNull();
                if (issue == null) {
                    response.addFail(getString(ISSUE_DOES_NOT_EXIST, thesaurus), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
                } else if (issueRef.getVersion() != issue.getVersion()){
                    response.addFail(getString(ISSUE_WAS_ALREADY_CHANGED, thesaurus), issueRef.getId(), issue.getTitle());
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
