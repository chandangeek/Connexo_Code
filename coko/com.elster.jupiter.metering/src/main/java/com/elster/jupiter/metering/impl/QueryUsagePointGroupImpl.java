package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.QueryUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;
import java.util.List;

public class QueryUsagePointGroupImpl implements QueryUsagePointGroup {

    private long id;

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
    public List<UsagePoint> getMembers(Date date) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public boolean isMember(UsagePoint usagePoint, Date date) {
        //TODO automatically generated method body, provide implementation.
        return false;
    }

    @Override
    public String getName() {
        return name;
    }
}
