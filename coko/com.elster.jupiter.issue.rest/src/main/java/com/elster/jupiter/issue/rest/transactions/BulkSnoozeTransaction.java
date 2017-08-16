/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.transactions;


import com.elster.jupiter.issue.rest.MessageSeeds;
import com.elster.jupiter.issue.rest.request.BulkSnoozeRequest;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public class BulkSnoozeTransaction implements Transaction<ActionInfo> {
    private final BulkSnoozeRequest request;
    private final User performer;
    private final Function<ActionInfo, List<? extends Issue>> issueProvider;
    private Thesaurus thesaurus;
    private Clock clock;

    public BulkSnoozeTransaction(BulkSnoozeRequest request, User performer, Function<ActionInfo, List<? extends Issue>> issueProvider, Thesaurus thesaurus, Clock clock) {
        this.request = request;
        this.performer = performer;
        this.issueProvider = issueProvider;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        for (Issue issue : issueProvider.apply(response)) {
            if (issue.getStatus().isHistorical()) {
                response.addFail(thesaurus.getFormat(MessageSeeds.ISSUE_ALREADY_CLOSED)
                        .format(), issue.getId(), issue.getTitle());
            } else if (issue.getStatus().getKey().equals(IssueStatus.IN_PROGRESS)) {
                response.addFail(thesaurus.getFormat(MessageSeeds.ISSUE_IN_PROGRESS)
                        .format(), issue.getId(), issue.getTitle());
            } else if (Instant.ofEpochMilli(request.snoozeDateTime).isBefore(Instant.now(clock))) {
                response.addFail(thesaurus.getFormat(MessageSeeds.SNOOZE_TIME_BEFORE_CURRENT_TIME)
                        .format(), issue.getId(), issue.getTitle());
            } else {
                issue.snooze(Instant.ofEpochMilli(request.snoozeDateTime));
                issue.addComment(request.comment, performer);
                issue.update();
                response.addSuccess(issue.getId());
            }
        }
        return response;
    }
}
