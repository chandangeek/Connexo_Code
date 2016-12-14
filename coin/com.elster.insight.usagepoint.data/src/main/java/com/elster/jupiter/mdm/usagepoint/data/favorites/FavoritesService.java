package com.elster.jupiter.mdm.usagepoint.data.favorites;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointGroup;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface FavoritesService {
    /*
     * Favorite usage points
     */
    List<FavoriteUsagePoint> getFavoriteUsagePoints();

    Optional<FavoriteUsagePoint> findFavoriteUsagePoint(UsagePoint usagePoint);

    FavoriteUsagePoint findOrCreateFavoriteUsagePoint(UsagePoint usagePoint);

    void removeFavoriteUsagePoint(FavoriteUsagePoint favoriteUsagePoint);

    /*
     * Favorite usage point groups
     */
    List<FavoriteUsagePointGroup> getFavoriteUsagePointGroups();
    
    Optional<FavoriteUsagePointGroup> findFavoriteUsagePointGroup(UsagePointGroup usagePointGroup);

   FavoriteUsagePointGroup findOrCreateFavoriteUsagePointGroup(UsagePointGroup usagePointGroup);
    
    void removeFavoriteUsagePointGroup(FavoriteUsagePointGroup favoriteUsagePointGroup);
}
