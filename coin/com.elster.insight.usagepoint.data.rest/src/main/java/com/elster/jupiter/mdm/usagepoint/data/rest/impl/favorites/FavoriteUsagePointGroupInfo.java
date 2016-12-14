package com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.UsagePointGroupInfo;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import java.time.Instant;
import java.util.Map;

public class FavoriteUsagePointGroupInfo {

    public boolean favorite;
    public String comment;
    public Instant creationDate;
    public UsagePointGroupInfo parent;

    public FavoriteUsagePointGroupInfo() {
    }

    public FavoriteUsagePointGroupInfo(UsagePointGroup usagePointGroup) {
        parent = new UsagePointGroupInfo();
        parent.id = usagePointGroup.getId();
        parent.version = usagePointGroup.getVersion();
        favorite = false;
    }

    public FavoriteUsagePointGroupInfo(FavoriteUsagePointGroup favoriteUsagePointGroup) {
        UsagePointGroup usagePointGroup = favoriteUsagePointGroup.getUsagePointGroup();
        parent = new UsagePointGroupInfo();
        parent.id = usagePointGroup.getId();
        parent.version = usagePointGroup.getVersion();
        favorite = true;
        comment = favoriteUsagePointGroup.getComment();
        creationDate = favoriteUsagePointGroup.getCreationDate();
    }

    public FavoriteUsagePointGroupInfo(UsagePointGroup usagePointGroup,
                                       Map<UsagePointGroup, FavoriteUsagePointGroup> favoriteGroupsMap) {
        parent = new UsagePointGroupInfo();
        parent.id = usagePointGroup.getId();
        parent.name = usagePointGroup.getName();
        parent.dynamic = usagePointGroup.isDynamic();
        parent.version = usagePointGroup.getVersion();
        FavoriteUsagePointGroup favoriteGroup = favoriteGroupsMap.get(usagePointGroup);
        favorite = favoriteGroup != null;
        if (favorite) {
            comment = favoriteGroup.getComment();
            creationDate = favoriteGroup.getCreationDate();
        }
    }
}
