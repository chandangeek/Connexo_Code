package com.elster.jupiter.issue.impl.records;


import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class IssueStatusImpl extends EntityImpl implements IssueStatus{
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 200, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_200 + "}")
    private String name;
    private boolean isFinal;

    @Inject
    public IssueStatusImpl(DataModel dataModel) {
        super(dataModel);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
}
