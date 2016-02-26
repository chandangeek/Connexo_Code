package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.groups.UsagePointGroup;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class UsagePointGroupInfoFactory {


    @Inject
    public UsagePointGroupInfoFactory() {
    }
    
    public UsagePointGroupInfo from(UsagePointGroup usagePointGroup) {
        UsagePointGroupInfo usagePointGroupInfo = new UsagePointGroupInfo();
        usagePointGroupInfo.id = usagePointGroup.getId();
        usagePointGroupInfo.mRID = usagePointGroup.getMRID();
        usagePointGroupInfo.name = usagePointGroup.getName();
        usagePointGroupInfo.dynamic = usagePointGroup.isDynamic();
        return usagePointGroupInfo;
    }

    public List<UsagePointGroupInfo> from(List<UsagePointGroup> usagePointGroups) {
        List<UsagePointGroupInfo> usagePointGroupsInfos = new ArrayList<>();
        for (UsagePointGroup usagePointGroup : usagePointGroups) {
            usagePointGroupsInfos.add(from(usagePointGroup));
        }
        return usagePointGroupsInfos;
    }
}
