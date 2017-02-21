/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;

import java.util.List;
import java.util.stream.Collectors;

public class UsagePointGroupInfoFactory {
    
    public UsagePointGroupInfo from(UsagePointGroup usagePointGroup) {
        UsagePointGroupInfo usagePointGroupInfo = new UsagePointGroupInfo();
        usagePointGroupInfo.id = usagePointGroup.getId();
        usagePointGroupInfo.mRID = usagePointGroup.getMRID();
        usagePointGroupInfo.name = usagePointGroup.getName();
        usagePointGroupInfo.dynamic = usagePointGroup.isDynamic();
        usagePointGroupInfo.version = usagePointGroup.getVersion();
        if (usagePointGroup.isDynamic()) {
            QueryUsagePointGroup queryUsagePointGroup = (QueryUsagePointGroup) usagePointGroup;
            usagePointGroupInfo.filter = SearchablePropertyValueConverter.convert(queryUsagePointGroup.getSearchablePropertyValues());
        }
        return usagePointGroupInfo;
    }

    public List<UsagePointGroupInfo> from(List<UsagePointGroup> usagePointGroups) {
        return usagePointGroups.stream()
                .map(this::from)
                .collect(Collectors.toList());
    }
}
