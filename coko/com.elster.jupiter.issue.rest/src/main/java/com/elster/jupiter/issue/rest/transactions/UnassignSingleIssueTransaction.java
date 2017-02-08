/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;

import java.util.function.Function;


public class UnassignSingleIssueTransaction implements Transaction<ActionInfo> {
    private final Function<ActionInfo, Issue> issueProvider;
    private final Thesaurus thesaurus;

    public UnassignSingleIssueTransaction(Function<ActionInfo, Issue> issueProvider, Thesaurus thesaurus) {
        this.issueProvider = issueProvider;
        this.thesaurus = thesaurus;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();

        Issue issue = issueProvider.apply(response);
        Long userId = -1L;
        Long workGroupId = issue.getAssignee().getWorkGroup() != null ? issue.getAssignee().getWorkGroup().getId() : -1L;
        issue.assignTo(userId, workGroupId);
        issue.update();
        response.addSuccess(issue.getId(), thesaurus.getFormat(MessageSeeds.ACTION_ISSUE_WAS_UNASSIGNED).format());

        return response;
    }
}