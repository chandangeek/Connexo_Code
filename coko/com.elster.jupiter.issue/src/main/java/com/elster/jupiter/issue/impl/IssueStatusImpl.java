package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.IssueStatus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

public class IssueStatusImpl implements IssueStatus {
    private final DataModel dataModel;

    private long id;
    private String name;

    // Audit fields
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    @Inject
    IssueStatusImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    IssueStatusImpl init(String name) {
        this.name = name;
        return this;
    }

    static IssueStatusImpl from(DataModel dataModel, String name) {
        return dataModel.getInstance(IssueStatusImpl.class).init(name);
    }

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
