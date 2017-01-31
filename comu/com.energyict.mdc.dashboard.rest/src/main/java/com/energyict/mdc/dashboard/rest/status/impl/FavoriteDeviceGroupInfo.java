/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.VersionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Comparator;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteDeviceGroupInfo {
    
    public static Comparator<FavoriteDeviceGroupInfo> byNameComparator = (info1, info2) -> info1.name.compareTo(info2.name);
    
    public long id;
    public String mRID;
    public String name;
    public boolean dynamic;
    public boolean favorite;
    public long version;
    public VersionInfo<Long> parent;

    public static FavoriteDeviceGroupInfo asInfo(EndDeviceGroup endDeviceGroup) {
        FavoriteDeviceGroupInfo info = new FavoriteDeviceGroupInfo();
        info.id = endDeviceGroup.getId();
        info.mRID = endDeviceGroup.getMRID();
        info.name = endDeviceGroup.getName();
        info.dynamic = endDeviceGroup.isDynamic();
        info.favorite = true;
        info.parent = new VersionInfo<>(info.id, endDeviceGroup.getVersion());
        return info;
    }

    public static FavoriteDeviceGroupInfo asInfo(EndDeviceGroup endDeviceGroup, List<EndDeviceGroup> favoriteEndDeviceGroups) {
        FavoriteDeviceGroupInfo info = asInfo(endDeviceGroup);
        info.favorite = favoriteEndDeviceGroups.stream().anyMatch(edg -> edg.getId() == endDeviceGroup.getId());
        if (info.favorite){
            info.version = 1;
        }
        return info;
    }
    
    public static class SelectionInfo {
        public List<FavoriteDeviceGroupInfo> ids;
    }
}
