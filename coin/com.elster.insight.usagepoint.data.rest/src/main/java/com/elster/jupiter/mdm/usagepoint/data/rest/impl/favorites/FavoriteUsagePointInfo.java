package com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.UsagePointInfo;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;
import java.util.Map;

public class FavoriteUsagePointInfo {

    public boolean favorite;
    public String comment;
    public Instant creationDate;
    public UsagePointInfo parent;

    public FavoriteUsagePointInfo() {
    }

    public FavoriteUsagePointInfo(UsagePoint usagePoint) {
        parent = new UsagePointInfo();
        parent.id = usagePoint.getId();
        parent.version = usagePoint.getVersion();
        favorite = false;
    }

    public FavoriteUsagePointInfo(FavoriteUsagePoint favoriteUsagePoint) {
        UsagePoint usagePoint = favoriteUsagePoint.getUsagePoint();
        parent = new UsagePointInfo();
        parent.id = usagePoint.getId();
        parent.version = usagePoint.getVersion();
        favorite = true;
        comment = favoriteUsagePoint.getComment();
        creationDate = favoriteUsagePoint.getCreationDate();
    }

    public FavoriteUsagePointInfo(UsagePoint usagePoint, Map<UsagePoint, FavoriteUsagePoint> favoritesMap) {
        parent = new UsagePointInfo();
        parent.id = usagePoint.getId();
        parent.name = usagePoint.getName();
        parent.version = usagePoint.getVersion();
        FavoriteUsagePoint favoriteUsagePoint = favoritesMap.get(usagePoint);
        favorite = favoriteUsagePoint != null;
        if (favorite) {
            comment = favoriteUsagePoint.getComment();
            creationDate = favoriteUsagePoint.getCreationDate();
        }
    }
}
