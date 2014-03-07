package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.issue.impl.database.NonSearchable;
import com.elster.jupiter.util.time.UtcInstant;

public class Entity {
    @NonSearchable
    protected long id;

    // Audit fields
    @NonSearchable
    protected long version;
    @NonSearchable
    protected UtcInstant createTime;
    @NonSearchable
    protected UtcInstant modTime;
    protected String userName;

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }


    public long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    public UtcInstant getCreateTime() {
        return createTime;
    }

    void setCreateTime(UtcInstant createTime) {
        this.createTime = createTime;
    }

    public UtcInstant getModTime() {
        return modTime;
    }

    void setModTime(UtcInstant modTime) {
        this.modTime = modTime;
    }

    public String getUserName() {
        return userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }
}
