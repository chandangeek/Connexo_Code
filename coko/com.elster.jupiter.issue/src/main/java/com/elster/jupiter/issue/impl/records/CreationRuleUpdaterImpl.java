package com.elster.jupiter.issue.impl.records;

import java.util.Map;

import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleUpdater;
import com.elster.jupiter.orm.DataModel;

public class CreationRuleUpdaterImpl extends CreationRuleBuilderImpl implements CreationRuleUpdater {

    public CreationRuleUpdaterImpl(DataModel dataModel, CreationRuleImpl creationRule) {
        super(dataModel, creationRule);
    }
    
    @Override
    public CreationRuleUpdater removeActions() {
        this.underConstruction.removeActions();
        return this;
    }
}
