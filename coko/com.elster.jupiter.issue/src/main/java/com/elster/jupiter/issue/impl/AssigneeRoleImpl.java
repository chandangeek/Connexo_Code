package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.AssigneeRole;
import com.elster.jupiter.util.time.UtcInstant;

public class AssigneeRoleImpl implements AssigneeRole {
    protected long id;
    protected String name;
    protected String description;

    // Audit fields
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public UtcInstant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(UtcInstant createTime) {
        this.createTime = createTime;
    }

    public UtcInstant getModTime() {
        return modTime;
    }

    public void setModTime(UtcInstant modTime) {
        this.modTime = modTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
