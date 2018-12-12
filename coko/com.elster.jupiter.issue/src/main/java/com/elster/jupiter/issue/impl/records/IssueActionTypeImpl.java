/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

public class IssueActionTypeImpl extends EntityImpl implements IssueActionType {

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 1024, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String className;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String factoryId;

    private Reference<IssueType> issueType = ValueReference.absent();
    private Reference<IssueReason> issueReason = ValueReference.absent();
    private CreationRuleActionPhase phase;

    private IssueActionService issueActionService;

    @Inject
    public IssueActionTypeImpl(DataModel dataModel, IssueActionService issueActionService) {
        super(dataModel);
        this.issueActionService = issueActionService;
    }

    public IssueActionTypeImpl init(String factoryId, String actionTypeClass, IssueReason issueReason, CreationRuleActionPhase phase){
        IssueType type = null;
        if (issueReason != null){
            type = issueReason.getIssueType();
        }
        return init(factoryId, actionTypeClass, issueReason, type, phase);
    }

    public IssueActionTypeImpl init(String factoryId, String actionTypeClass, IssueType issueType, CreationRuleActionPhase phase){
        return init(factoryId, actionTypeClass, null, issueType, phase);
    }

    private IssueActionTypeImpl init(String factoryId, String actionTypeClass, IssueReason issueReason, IssueType issueType, CreationRuleActionPhase phase) {
        this.factoryId = factoryId;
        this.className = actionTypeClass;
        this.issueReason.set(issueReason);
        this.issueType.set(issueType);
        this.phase = phase;
        return this;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getFactoryId() {
        return factoryId;
    }

    @Override
    public IssueType getIssueType() {
        return issueType.orNull();
    }

    @Override
    public IssueReason getIssueReason() {
        return issueReason.orNull();
    }

    @Override
    public Optional<IssueAction> createIssueAction() {
        return issueActionService.createIssueAction(factoryId, className);
    }

    @Override
    public CreationRuleActionPhase getPhase() {
        return phase;
    }
}
