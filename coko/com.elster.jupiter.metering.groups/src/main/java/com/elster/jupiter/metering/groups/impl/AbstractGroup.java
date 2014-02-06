package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.util.time.UtcInstant;

public class AbstractGroup {

    protected long id;
    private String name;
    private String mRID;
    private String description;
    private String aliasName;
    private String type;
    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    public String getAliasName() {
        return aliasName;
    }

    public String getDescription() {
        return description;
    }

    public String getMRID() {
        return mRID;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMRID(String mrid) {
        this.mRID = mrid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    final void setType(String type) {
        this.type = type;
    }
}
