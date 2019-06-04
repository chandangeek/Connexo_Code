/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

public final class OpenIssueImpl extends IssueImpl implements OpenIssue {

    @Inject
    public OpenIssueImpl(DataModel dataModel, IssueService issueService, Clock clock, Thesaurus thesaurus) {
        super(dataModel, issueService, clock, thesaurus);
    }

    @Override
    public HistoricalIssue close(IssueStatus status) {
        return closeInternal(status);
    }

    public HistoricalIssue closeInternal(IssueStatus status) {
        if (status == null || !status.isHistorical()) {
            throw new IllegalArgumentException("Incorrect status for closing issue");
        }
        this.setStatus(status);
        HistoricalIssueImpl historicalIssue = getDataModel().getInstance(HistoricalIssueImpl.class);
        historicalIssue.copy(this);
        historicalIssue.save();
        this.delete();
        return historicalIssue;
    }
}
