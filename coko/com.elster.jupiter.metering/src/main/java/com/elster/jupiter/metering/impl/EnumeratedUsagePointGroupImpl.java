package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;
import java.util.List;

public class EnumeratedUsagePointGroupImpl implements EnumeratedUsagePointGroup {

    private String name;
    private String mrid;
    private String description;
    private String aliasName;
    private String type;

    @Override
    public Entry add(UsagePoint usagePoint, Interval interval) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public void remove(Entry entry) {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public long getId() {
        //TODO automatically generated method body, provide implementation.
        return 0;
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
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mrid;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMRID(String mrid) {
        this.mrid = mrid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setType(String type) {
        this.type = type;
    }
}
