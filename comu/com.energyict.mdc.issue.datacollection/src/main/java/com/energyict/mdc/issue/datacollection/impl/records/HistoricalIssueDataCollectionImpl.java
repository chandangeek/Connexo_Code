package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

import javax.inject.Inject;

public class HistoricalIssueDataCollectionImpl extends IssueDataCollectionImpl implements HistoricalIssueDataCollection {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<HistoricalIssue> baseIssue = ValueReference.absent();

    @Inject
    public HistoricalIssueDataCollectionImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    protected HistoricalIssue getBaseIssue(){
        return baseIssue.orNull();
    }

    @Override
    public <T extends Issue> void setIssue(T issue) {
        if (issue instanceof HistoricalIssue) {
            this.baseIssue.set((HistoricalIssue) issue);
        } else {
            super.setIssue(issue);
        }
    }
}
