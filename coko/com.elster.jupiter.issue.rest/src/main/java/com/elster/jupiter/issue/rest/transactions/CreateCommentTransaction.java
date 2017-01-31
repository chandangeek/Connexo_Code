/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.transactions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

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
        Issue issue = issueService.findIssue(issueId).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        return issue.addComment(comment, author).orElse(null);
    }
}
