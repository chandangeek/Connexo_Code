/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.transactions;


import com.elster.jupiter.issue.rest.request.BulkSnoozeRequest;
import com.elster.jupiter.issue.rest.response.ActionInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

public class BulkSnoozeTransaction implements Transaction<ActionInfo> {
    private final BulkSnoozeRequest request;
    private final User performer;
    private final Function<ActionInfo, List<? extends Issue>> issueProvider;

    public BulkSnoozeTransaction(BulkSnoozeRequest request, User performer, Function<ActionInfo, List<? extends Issue>> issueProvider) {
        this.request = request;
        this.performer = performer;
        this.issueProvider = issueProvider;
    }

    @Override
    public ActionInfo perform() {
        ActionInfo response = new ActionInfo();
        for (Issue issue : issueProvider.apply(response)) {
            issue.snooze(Instant.ofEpochMilli(request.snoozeDateTime));
            issue.addComment(request.comment, performer);
            issue.update();
            response.addSuccess(issue.getId());
        }
        return response;
    }
}
