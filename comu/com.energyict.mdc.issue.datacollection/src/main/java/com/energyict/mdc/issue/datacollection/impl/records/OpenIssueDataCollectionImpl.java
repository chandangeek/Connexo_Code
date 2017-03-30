/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import javax.inject.Inject;

public class OpenIssueDataCollectionImpl extends IssueDataCollectionImpl implements OpenIssueDataCollection {

    @IsPresent
    private Reference<OpenIssue> baseIssue = ValueReference.absent();

    @Inject
    public OpenIssueDataCollectionImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    protected OpenIssue getBaseIssue(){
        return baseIssue.orNull();
    }

    public void setIssue(OpenIssue baseIssue) {
        this.baseIssue.set(baseIssue);
    }

    public HistoricalIssueDataCollection close(IssueStatus status) {
        this.delete(); // Remove reference to baseIssue
        HistoricalIssue historicalBaseIssue = getBaseIssue().closeInternal(status);
        HistoricalIssueDataCollectionImpl historicalDataCollectionIssue = getDataModel().getInstance(HistoricalIssueDataCollectionImpl.class);
        historicalDataCollectionIssue.setIssue(historicalBaseIssue);
        historicalDataCollectionIssue.copy(this);
        historicalDataCollectionIssue.save();
        return historicalDataCollectionIssue;
    }
}
