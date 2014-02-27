package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

public class HistoricalIssueImpl extends IssueImpl implements HistoricalIssue {

    private String issueType;

    @Inject
    HistoricalIssueImpl(DataModel dataModel) {
        super(dataModel);
    }

    HistoricalIssueImpl init(IssueImpl issue) {
        super.init(issue);
        issueType = IssueImpl.TYPE_IDENTIFIER;
        return this;
    }

    static HistoricalIssueImpl from(DataModel dataModel, IssueImpl issue) {
        return dataModel.getInstance(HistoricalIssueImpl.class).init(issue);
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }
}
