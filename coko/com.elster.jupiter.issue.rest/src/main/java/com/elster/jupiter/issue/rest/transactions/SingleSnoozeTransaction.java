/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.transactions;

import com.elster.jupiter.issue.rest.TranslationKeys;
import com.elster.jupiter.issue.rest.request.SingleSnoozeRequest;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.function.Function;


public class SingleSnoozeTransaction implements Transaction<ActionInfo> {
    private final SingleSnoozeRequest request;
    private final User performer;
    private final Function<ActionInfo, Issue> issueProvider;
    private final Thesaurus thesaurus;

    public SingleSnoozeTransaction(SingleSnoozeRequest request, User performer, Function<ActionInfo, Issue> issueProvider, Thesaurus thesaurus) {
        this.request = request;
        this.performer = performer;
        this.issueProvider = issueProvider;
        this.thesaurus = thesaurus;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        Issue issue = issueProvider.apply(response);
        issue.snooze(Instant.ofEpochMilli(request.snoozeDateTime));
        issue.addComment(request.comment, performer);
        issue.update();
        response.addSuccess(issue.getId(), thesaurus.getFormat(TranslationKeys.ISSUE_ACTION_SNOOZED).format());
        return response;
    }
}