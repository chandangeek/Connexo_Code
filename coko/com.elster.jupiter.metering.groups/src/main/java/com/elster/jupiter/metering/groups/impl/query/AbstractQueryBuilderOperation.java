package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.metering.groups.impl.UsagePointQueryBuilderOperation;

public abstract class AbstractQueryBuilderOperation implements UsagePointQueryBuilderOperation {

    private int position;
    private long groupId;
    private Object group;

    @Override
    public void setPosition(int i) {
        this.position = i;
    }

    @Override
    public void setGroupId(long id) {
        groupId = id;
    }
}
