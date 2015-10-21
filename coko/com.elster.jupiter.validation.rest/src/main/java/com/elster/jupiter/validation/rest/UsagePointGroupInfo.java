package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.groups.UsagePointGroup;

public class UsagePointGroupInfo {

    public long id;
    public String mRID;
    public String name;

    public UsagePointGroupInfo() {
    }

    public UsagePointGroupInfo(UsagePointGroup usagePointGroup) {
        this.id = usagePointGroup.getId();
        this.mRID = usagePointGroup.getMRID();
        this.name = usagePointGroup.getName();
    }

}
