package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class CreateCommentTransaction implements Transaction<IssueComment> {
    private IssueService issueService;
    private long issueId;
    private String comment;
    private User author;

    public CreateCommentTransaction(long issueId, String comment, User author, IssueService issueService) {
        this.issueId = issueId;
        this.comment = comment;
        this.author = author;
        this.issueService = issueService;
    }

    @Override
    public IssueComment perform() {
        Optional<Issue> issueRef = issueService.findIssue(issueId, true);
        IssueComment issueComment = null;
        if (issueRef.isPresent()){
            Issue issue = issueRef.get();
            issueComment = issue.addComment(comment, author).orNull();
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return issueComment;
    }
}
