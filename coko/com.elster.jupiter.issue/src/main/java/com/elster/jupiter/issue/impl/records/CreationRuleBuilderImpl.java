package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleActionBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.orm.DataModel;

import java.util.Map;

public class CreationRuleBuilderImpl implements CreationRuleBuilder {

    protected final CreationRuleImpl underConstruction;
    private final DataModel dataModel;

    public CreationRuleBuilderImpl(DataModel dataModel, CreationRuleImpl creationRule) {
        this.dataModel = dataModel;
        this.underConstruction = creationRule;
    }
    
    @Override
    public CreationRuleBuilder setName(String name) {
        this.underConstruction.setName(name);
        return this;
    }
    
    @Override
    public CreationRuleBuilder setComment(String comment) {
        this.underConstruction.setComment(comment);
        return this;
    }

    @Override
    public CreationRuleBuilder setIssueType(IssueType issueType) {
        this.underConstruction.setIssueType(issueType);
        return this;
    }

    @Override
    public CreationRuleBuilder setReason(IssueReason reason) {
        this.underConstruction.setReason(reason);
        return this;
    }

    @Override
    public CreationRuleBuilder setDueInTime(DueInType dueInType, long dueInValue) {
        this.underConstruction.setDueInType(dueInType);
        this.underConstruction.setDueInValue(dueInValue);
        return this;
    }

    @Override
    public CreationRuleBuilder setTemplate(String name) {
        this.underConstruction.setTemplate(name);
        return this;
    }
    
    @Override
    public CreationRuleBuilder setProperties(Map<String, Object> props) {
        this.underConstruction.setProperties(props);
        return this;
    }

    @Override
    public CreationRuleActionBuilder newCreationRuleAction() {
        return new CreationRuleActionBuilderImpl(dataModel.getInstance(CreationRuleActionImpl.class), underConstruction);
    }

    @Override
    public CreationRule complete() {
        return this.underConstruction;
    }
    
    private class CreationRuleActionBuilderImpl implements CreationRuleActionBuilder {

        private final CreationRuleActionImpl underConstruction;
        
        public CreationRuleActionBuilderImpl(CreationRuleActionImpl action, CreationRule creationRule) {
            this.underConstruction = action;
            this.underConstruction.setRule(creationRule);
        }
        
        @Override
        public CreationRuleActionBuilder setActionType(IssueActionType issueActionType) {
            underConstruction.setAction(issueActionType);
            return this;
        }
        
        @Override
        public CreationRuleActionBuilder setPhase(CreationRuleActionPhase phase) {
            this.underConstruction.setPhase(phase);
            return this;
        }

        @Override
        public CreationRuleActionBuilder addProperty(String name, Object value) {
            this.underConstruction.addProperty(name, value);
            return this;
        }

        @Override
        public CreationRuleAction complete() {
            CreationRuleBuilderImpl.this.underConstruction.addAction(underConstruction);
            return this.underConstruction;
        }
    }
}
