package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRuleActionType;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreationRuleActionTypeImpl extends EntityImpl implements CreationRuleActionType{

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 256, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_256 + "}")
    private String name;

    @Size(max = 256, message = "{" + MessageSeeds.Keys.ACTION_TYPE_DESCRIPTION_SIZE + "}")
    private String description;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 1024, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_1024 + "}")
    private String className;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueType> issueType = ValueReference.absent();

    @Inject
    public CreationRuleActionTypeImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public IssueType getIssueType() {
        return issueType.orNull();
    }

    @Override
    public void setIssueType(IssueType type) {
        issueType.set(type);
    }
}
