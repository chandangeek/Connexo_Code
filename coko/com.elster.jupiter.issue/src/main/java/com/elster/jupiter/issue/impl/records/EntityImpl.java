/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;
import java.util.Objects;

public abstract class EntityImpl implements Entity {

    private long id;

    // Audit fields
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private DataModel dataModel;

    protected EntityImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void save() {
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public void update() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityImpl that = (EntityImpl) o;

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
