/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.CreationRuleExclGroup;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleActionBuilder;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public CreationRuleBuilder setPriority(Priority priority) {
        this.underConstruction.setPriority(priority);
        return this;
    }

    public CreationRuleBuilder activate() {
        this.underConstruction.activate();
        return this;
    }

    @Override
    public CreationRuleBuilder deactivate() {
        this.underConstruction.deactivate();
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
    public CreationRuleBuilder setExcludedDeviceGroups(List<EndDeviceGroup> deviceGroupsList) {
        final List<CreationRuleExclGroup> list = deviceGroupsList.stream().map(group -> {
            CreationRuleExclGroup mapping = dataModel.getInstance(CreationRuleExclGroupImpl.class);
            mapping.setCreationRule(underConstruction);
            mapping.setEndDeviceGroup(group);
            return mapping;
        }).collect(Collectors.toList());
        underConstruction.setExcludedDeviceGroupList(list);
        return this;
    }

    @Override
    public CreationRule complete() {
        this.underConstruction.save();
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
