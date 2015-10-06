package com.elster.insight.usagepoint.data.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.elster.jupiter.metering.UsagePoint;

public class UsagePointGroupMemberInfo {

    public long id;
    public String mRID;
    public String serviceKind;
    public String name;
    
    public static UsagePointGroupMemberInfo from(UsagePoint usagePoint) {
        UsagePointGroupMemberInfo usagePointMemberInfo = new UsagePointGroupMemberInfo();
        usagePointMemberInfo.id = usagePoint.getId();
        usagePointMemberInfo.name = usagePoint.getName();
        usagePointMemberInfo.mRID = usagePoint.getMRID();
        usagePointMemberInfo.serviceKind = usagePoint.getServiceCategory().getName();
        return usagePointMemberInfo;
    }
    
    public static List<UsagePointGroupMemberInfo> from(List<UsagePoint> usagePoint) {
        return usagePoint.stream().map((u) -> from(u)).collect(Collectors.toList());
    }
}
