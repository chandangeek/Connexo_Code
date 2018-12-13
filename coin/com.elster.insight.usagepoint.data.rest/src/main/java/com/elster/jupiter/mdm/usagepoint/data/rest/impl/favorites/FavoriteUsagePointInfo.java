/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.VersionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavoriteUsagePointInfo {

    public boolean favorite;
    public String comment;
    public Instant creationDate;
    public VersionInfo<Long> parent;

    public FavoriteUsagePointInfo() {
    }

    public FavoriteUsagePointInfo(UsagePoint usagePoint) {
        parent = new VersionInfo<>();
        parent.id = usagePoint.getId();
        parent.version = usagePoint.getVersion();
        favorite = false;
    }

    public FavoriteUsagePointInfo(FavoriteUsagePoint favoriteUsagePoint) {
        UsagePoint usagePoint = favoriteUsagePoint.getUsagePoint();
        parent = new VersionInfo<>();
        parent.id = usagePoint.getId();
        parent.version = usagePoint.getVersion();
        favorite = true;
        comment = favoriteUsagePoint.getComment();
        creationDate = favoriteUsagePoint.getCreationDate();
    }

    public FavoriteUsagePointInfo(UsagePoint usagePoint, Map<UsagePoint, FavoriteUsagePoint> favoritesMap) {
        parent = new VersionInfo<>();
        parent.id = usagePoint.getId();
        parent.version = usagePoint.getVersion();
//        usagePointInfo.name = usagePoint.getName();
        FavoriteUsagePoint favoriteUsagePoint = favoritesMap.get(usagePoint);
        favorite = favoriteUsagePoint != null;
        if (favorite) {
            comment = favoriteUsagePoint.getComment();
            creationDate = favoriteUsagePoint.getCreationDate();
        }
    }
}
