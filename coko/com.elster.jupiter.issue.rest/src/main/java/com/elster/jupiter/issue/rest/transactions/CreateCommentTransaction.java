package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class CreateCommentTransaction implements Transaction<IssueComment> {
    private IssueMainService issueMainService;
    private long issueId;
    private String comment;
    private User author;

    public CreateCommentTransaction(long issueId, String comment, User author, IssueMainService issueMainService) {
        this.issueId = issueId;
        this.comment = comment;
        this.author = author;
        this.issueMainService = issueMainService;
    }

    @Override
    public IssueComment perform() {
        Optional<IssueComment> commentRef = issueMainService.save(new IssueComment(issueId, comment, author));
        if(!commentRef.isPresent()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        return commentRef.get();
    }
}
