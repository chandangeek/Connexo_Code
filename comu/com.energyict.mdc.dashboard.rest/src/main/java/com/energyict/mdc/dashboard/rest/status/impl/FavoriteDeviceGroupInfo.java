package com.energyict.mdc.dashboard.rest.status.impl;

import java.util.Comparator;
import java.util.List;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

public class FavoriteDeviceGroupInfo {
    
    public static Comparator<FavoriteDeviceGroupInfo> byNameComparator = (info1, info2) -> info1.name.compareTo(info2.name);
    
    public long id;
    public String mRID;
    public String name;
    public boolean dynamic;
    public boolean favorite;
    
    public static FavoriteDeviceGroupInfo asInfo(EndDeviceGroup endDeviceGroup) {
        FavoriteDeviceGroupInfo info = new FavoriteDeviceGroupInfo();
        info.id = endDeviceGroup.getId();
        info.mRID = endDeviceGroup.getMRID();
        info.name = endDeviceGroup.getName();
        info.dynamic = endDeviceGroup.isDynamic();
        info.favorite = true;
        return info;
    }

    public static FavoriteDeviceGroupInfo asInfo(EndDeviceGroup endDeviceGroup, List<EndDeviceGroup> favoriteEndDeviceGroups) {
        FavoriteDeviceGroupInfo info = asInfo(endDeviceGroup);
        info.favorite = favoriteEndDeviceGroups.stream().anyMatch(edg -> edg.getId() == endDeviceGroup.getId());
        return info;
    }
    
    public static class SelectionInfo {
        public List<Long> ids;
    }
}
