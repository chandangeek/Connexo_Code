package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Optional;

public class OpenIssueImpl extends IssueImpl implements OpenIssue {

    @Inject
    public OpenIssueImpl(DataModel dataModel, IssueService issueService) {
        super(dataModel, issueService);
    }

    @Override
    public HistoricalIssue close(IssueStatus status) {
        for (IssueProvider issueProvider : getIssueService().getIssueProviders()) {
            Optional<? extends OpenIssue> issue = issueProvider.getOpenIssue(this);
            if (issue.isPresent()) {
                return issue.get().close(status);
            }
        }
        return null;
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
