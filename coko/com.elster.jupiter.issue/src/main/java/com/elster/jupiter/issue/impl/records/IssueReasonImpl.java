package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class IssueReasonImpl extends EntityImpl implements IssueReason{
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 200, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_200 + "}")
    private String name;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueType> issueType = ValueReference.absent();

    @Inject
    public IssueReasonImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
