package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import javax.inject.Inject;

public class OpenIssueDataCollectionImpl extends IssueDataCollectionImpl implements OpenIssueDataCollection {

    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Inject
    public OpenIssueDataCollectionImpl(DataModel dataModel, IssueService issueService) {
        super(dataModel, issueService);
    }

    @Override
    protected OpenIssue getBaseIssue(){
        return baseIssue.orNull();
    }

    @Override
    public <T extends Issue> void setIssue(T issue) {
        if (issue instanceof OpenIssue) {
            this.baseIssue.set((OpenIssue) issue);
        } else {
            super.setIssue(issue);
        }
    }
}
