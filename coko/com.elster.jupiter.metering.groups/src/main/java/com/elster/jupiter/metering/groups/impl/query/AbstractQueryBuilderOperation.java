package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.metering.groups.impl.UsagePointQueryBuilderOperation;

import java.time.Instant;

abstract class AbstractQueryBuilderOperation implements UsagePointQueryBuilderOperation {

    @SuppressWarnings("unused") // Managed by ORM
    private int position;
    @SuppressWarnings("unused") // Managed by ORM
    private long groupId;
    private Object group;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Override
    public void setPosition(int i) {
        this.position = i;
    }

    @Override
    public void setGroupId(long id) {
        groupId = id;
    }

}