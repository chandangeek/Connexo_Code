package com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.VersionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteUsagePointGroupDetailsInfo {

    public Long id;
    public String name;
    public boolean dynamic;
    public Instant flaggedDate;
    public String comment;
    public boolean favorite;
    public VersionInfo<Long> parent;

    public FavoriteUsagePointGroupDetailsInfo() {
    }

    public Long getId() {
        return id;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public FavoriteUsagePointGroupDetailsInfo(FavoriteUsagePointGroup favoriteUsagePointGroup) {
        UsagePointGroup usagePointGroup = favoriteUsagePointGroup.getUsagePointGroup();
        id = usagePointGroup.getId();
        name = usagePointGroup.getName();
        dynamic = usagePointGroup.isDynamic();
        comment = favoriteUsagePointGroup.getComment();
        flaggedDate = favoriteUsagePointGroup.getCreationDate();
        favorite = true;
        parent = new VersionInfo<>();
        parent.id = usagePointGroup.getId();
        parent.version = usagePointGroup.getVersion();
    }

    public FavoriteUsagePointGroupDetailsInfo(UsagePointGroup usagePointGroup, List<FavoriteUsagePointGroup> favoriteUsagePointGroups) {
        //UsagePointGroup usagePointGroup = favoriteUsagePointGroup.getUsagePointGroup();
        id = usagePointGroup.getId();
        name = usagePointGroup.getName();
        dynamic = usagePointGroup.isDynamic();
        favorite = favoriteUsagePointGroups.stream().anyMatch(fupg -> usagePointGroup.getId() == fupg.getUsagePointGroup().getId());
        ;
        parent = new VersionInfo<>();
        parent.id = usagePointGroup.getId();
        parent.version = usagePointGroup.getVersion();
    }

    public FavoriteUsagePointGroupInfo toFavoriteUsagePointGroupInfo() {
        FavoriteUsagePointGroupInfo favoriteUsagePointGroupInfo = new FavoriteUsagePointGroupInfo();

        favoriteUsagePointGroupInfo.favorite = favorite;
        favoriteUsagePointGroupInfo.comment = comment;
        favoriteUsagePointGroupInfo.parent = parent;
        return favoriteUsagePointGroupInfo;
    }

    public static class FavoriteUsagePointGroups {
        public List<FavoriteUsagePointGroupDetailsInfo> favoriteUsagePointGroups;
    }
}
