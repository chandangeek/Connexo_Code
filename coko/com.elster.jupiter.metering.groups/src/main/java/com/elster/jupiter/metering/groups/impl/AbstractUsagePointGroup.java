package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public abstract class AbstractUsagePointGroup implements UsagePointGroup {
    // ORM inheritance map
    static final Map<String, Class<? extends UsagePointGroup>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends UsagePointGroup>>of(QueryUsagePointGroup.TYPE_IDENTIFIER, QueryUsagePointGroupImpl.class, EnumeratedUsagePointGroup.TYPE_IDENTIFIER, EnumeratedUsagePointGroupImpl.class);

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

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
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
