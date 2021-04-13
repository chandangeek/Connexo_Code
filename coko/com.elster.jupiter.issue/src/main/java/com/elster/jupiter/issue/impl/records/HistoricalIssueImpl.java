/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.time.Clock;

public final class HistoricalIssueImpl extends IssueImpl implements HistoricalIssue {

    @Inject
    public HistoricalIssueImpl(DataModel dataModel, IssueService issueService, Clock clock, Thesaurus thesaurus) {
        super(dataModel, issueService, clock, thesaurus);
    }

    @Override
    public void delete() {
        if (getType() == null || !IssueService.MANUAL_ISSUE_TYPE.equals(getType().getKey())) {
            this.getIssueService().getIssueProviders().forEach(provider -> provider.getHistoricalIssue(this).ifPresent(Entity::delete));
        }
        this.getDataModel().remove(this);
    }
}
