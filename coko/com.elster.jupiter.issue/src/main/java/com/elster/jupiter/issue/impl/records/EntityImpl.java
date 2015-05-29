package com.elster.jupiter.issue.impl.records;

import java.time.Instant;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.orm.DataModel;

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

    void setId(long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    void setModTime(Instant modTime) {
        this.modTime = modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void save() {
        if (this.createTime == null) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }
}
