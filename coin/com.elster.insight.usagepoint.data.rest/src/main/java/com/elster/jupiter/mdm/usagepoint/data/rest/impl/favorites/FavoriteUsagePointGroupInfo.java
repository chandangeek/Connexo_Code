package com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.VersionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteUsagePointGroupInfo {

    public boolean favorite;
    public String comment;
    public Instant creationDate;
    public VersionInfo<Long> parent;

    public FavoriteUsagePointGroupInfo() {
    }

    public FavoriteUsagePointGroupInfo(UsagePointGroup usagePointGroup) {
        parent = new VersionInfo<>();
        parent.id = usagePointGroup.getId();
        parent.version = usagePointGroup.getVersion();
        favorite = false;
    }

    public FavoriteUsagePointGroupInfo(FavoriteUsagePointGroup favoriteUsagePointGroup) {
        UsagePointGroup usagePointGroup = favoriteUsagePointGroup.getUsagePointGroup();
        parent = new VersionInfo<>();
        parent.id = usagePointGroup.getId();
        parent.version = usagePointGroup.getVersion();
        favorite = true;
        comment = favoriteUsagePointGroup.getComment();
        creationDate = favoriteUsagePointGroup.getCreationDate();
    }

    public FavoriteUsagePointGroupInfo(UsagePointGroup usagePointGroup,
                                       Map<UsagePointGroup, FavoriteUsagePointGroup> favoriteGroupsMap) {
        parent = new VersionInfo<>();
        parent.id = usagePointGroup.getId();
        parent.version = usagePointGroup.getVersion();
//        usagePointGroupInfo.name = usagePointGroup.getName();
//        usagePointGroupInfo.dynamic = usagePointGroup.isDynamic();
        FavoriteUsagePointGroup favoriteGroup = favoriteGroupsMap.get(usagePointGroup);
        favorite = favoriteGroup != null;
        if (favorite) {
            comment = favoriteGroup.getComment();
            creationDate = favoriteGroup.getCreationDate();
        }
    }
}
