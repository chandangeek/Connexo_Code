/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.TranslationKeys;
import com.elster.jupiter.issue.rest.request.AssignSingleIssueRequest;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.function.Function;


public class AssignSingleIssueTransaction implements Transaction<ActionInfo> {
    private final AssignSingleIssueRequest request;
    private final User performer;
    private final Function<ActionInfo, Issue> issueProvider;
    private final Thesaurus thesaurus;

    public AssignSingleIssueTransaction(AssignSingleIssueRequest request, User performer, Function<ActionInfo, Issue> issueProvider, Thesaurus thesaurus) {
        this.request = request;
        this.performer = performer;
        this.issueProvider = issueProvider;
        this.thesaurus = thesaurus;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        if (request.assignee != null) {
            Issue issue = issueProvider.apply(response);
            issue.assignTo(request.assignee.userId, request.assignee.workGroupId);
            issue.addComment(request.comment, performer);
            issue.update();
            if(request.assignee.userId == -1L && request.assignee.workGroupId == -1L){
                response.addSuccess(issue.getId(), thesaurus.getFormat(TranslationKeys.ISSUE_ACTION_UNASSIGNED).format());
            } else {
                response.addSuccess(issue.getId(), thesaurus.getFormat(TranslationKeys.ISSUE_ACTION_ASSIGNED).format());
            }
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return response;
    }
}