package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.IssueAction;
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

public class IssueActionTypeImpl extends EntityImpl implements IssueActionType {

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 1024, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_1024 + "}")
    private String className;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String factoryId;

    private Reference<IssueType> issueType = ValueReference.absent();
    private Reference<IssueReason> issueReason = ValueReference.absent();

    private IssueActionService issueActionService;
    
    @Inject
    public IssueActionTypeImpl(DataModel dataModel, IssueActionService issueActionService) {
        super(dataModel);
        this.issueActionService = issueActionService;
    }

    public void init(String factoryId, String actionTypeClass, IssueReason issueReason){
        IssueType type = null;
        if (issueReason != null){
            type = issueReason.getIssueType();
        }
        this.init(factoryId, actionTypeClass, issueReason, type);
    }

    public void init(String factoryId, String actionTypeClass, IssueType issueType){
        this.init(factoryId, actionTypeClass, null, issueType);
    }

    private void init(String factoryId, String actionTypeClass, IssueReason issueReason, IssueType issueType){
        this.factoryId = factoryId;
        this.className = actionTypeClass;
        this.issueReason.set(issueReason);
        this.issueType.set(issueType);
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
    public IssueAction createIssueAction() {
        return issueActionService.createIssueAction(factoryId, className);
    }
}
