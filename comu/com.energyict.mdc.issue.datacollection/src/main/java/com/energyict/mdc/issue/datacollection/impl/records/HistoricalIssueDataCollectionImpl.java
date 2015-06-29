package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
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

    void setIssue(HistoricalIssue issue) {
        this.baseIssue.set(issue);
    }

    void copy(OpenIssueDataCollection source) {
        this.setId(source.getId());
        this.setCommunicationTask(source.getCommunicationTask().orElse(null));
        this.setConnectionTask(source.getConnectionTask().orElse(null));
        this.setComSession(source.getComSession().orElse(null));
        this.setDeviceMRID(source.getDeviceMRID());
    }
}
