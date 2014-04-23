package com.elster.jupiter.issue.rest.transactions;


import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.elster.jupiter.issue.rest.i18n.MessageSeeds.*;

public class AssignIssueTransaction  implements Transaction<ActionInfo> {
    private final IssueService issueService;
    private final AssignIssueRequest request;
    private final User author;
    private final Thesaurus thesaurus;

    public AssignIssueTransaction(AssignIssueRequest request, IssueService issueService, User author, Thesaurus thesaurus){
        this.request = request;
        this.issueService = issueService;
        this.author = author;
        this.thesaurus = thesaurus;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        if (request.getIssues() != null && request.getAssignee() != null) {
            for (EntityReference issueRef : request.getIssues()) {
                Issue issue = issueService.findIssue(issueRef.getId()).orNull();
                if (issue == null) {
                    response.addFail(getString(ISSUE_DOES_NOT_EXIST, thesaurus), issueRef.getId(), "Issue (id = " + issueRef.getId() + ")");
                } else if (issueRef.getVersion() != issue.getVersion()){
                    response.addFail(getString(ISSUE_WAS_ALREADY_CHANGED, thesaurus), issueRef.getId(), issue.getTitle());
                } else {
                    issue.assignTo(request.getAssignee().getType(), request.getAssignee().getId());
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
}
