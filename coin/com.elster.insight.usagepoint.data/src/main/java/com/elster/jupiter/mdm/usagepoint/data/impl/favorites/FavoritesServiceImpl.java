package com.elster.jupiter.mdm.usagepoint.data.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoritesService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class FavoritesServiceImpl implements FavoritesService {
    private final DataModel dataModel;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public FavoritesServiceImpl(UsagePointDataModelService usagePointDataModelService,
                                ThreadPrincipalService threadPrincipalService) {
        this.dataModel = usagePointDataModelService.dataModel();
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public List<FavoriteUsagePoint> getFavoriteUsagePoints() {
        User user = getUser();
        return user == null ? Collections.emptyList() : dataModel.query(FavoriteUsagePoint.class, UsagePoint.class)
                .select(where("user").isEqualTo(user).and(where("usagePoint.obsoleteTime").isNull()));
    }


    @Override
    public List<FavoriteUsagePointGroup> getFavoriteUsagePointGroups() {
        User user = getUser();
        return user == null ? Collections.emptyList() : dataModel.mapper(FavoriteUsagePointGroup.class).find("user", user);
    }

    @Override
    public Optional<FavoriteUsagePoint> findFavoriteUsagePoint(UsagePoint usagePoint) {
        return findFavoriteUsagePoint(usagePoint, getUser());
    }

    @Override
    public Optional<FavoriteUsagePointGroup> findFavoriteUsagePointGroup(UsagePointGroup usagePointGroup) {
        return findFavoriteUsagePointGroup(usagePointGroup, getUser());
    }

    @Override
    public FavoriteUsagePoint markFavorite(UsagePoint usagePoint) {
        User user = getUser();
        return findFavoriteUsagePoint(usagePoint, user)
                .orElseGet(() -> FavoriteUsagePointImpl.from(dataModel, usagePoint, user));
    }

    @Override
    public FavoriteUsagePointGroup markFavorite(UsagePointGroup usagePointGroup) {
        User user = getUser();
        return findFavoriteUsagePointGroup(usagePointGroup, user)
                .orElseGet(() -> FavoriteUsagePointGroupImpl.from(dataModel, usagePointGroup, user));
    }

    @Override
    public void removeFromFavorites(FavoriteUsagePoint favoriteUsagePoint) {
        dataModel.remove(favoriteUsagePoint);
    }

    @Override
    public void removeFromFavorites(FavoriteUsagePointGroup favoriteUsagePointGroup) {
        dataModel.remove(favoriteUsagePointGroup);
    }

    private User getUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        return principal instanceof User ? (User) principal : null;
    }

    private Optional<FavoriteUsagePoint> findFavoriteUsagePoint(UsagePoint usagePoint, User theUser) {
        return Optional.ofNullable(theUser)
                .flatMap(user -> dataModel.mapper(FavoriteUsagePoint.class)
                        .getUnique("usagePoint", usagePoint, "user", user));
    }

    private Optional<FavoriteUsagePointGroup> findFavoriteUsagePointGroup(UsagePointGroup usagePointGroup, User theUser) {
        return Optional.ofNullable(theUser)
                .flatMap(user -> dataModel.mapper(FavoriteUsagePointGroup.class)
                        .getUnique("usagePointGroup", usagePointGroup, "user", user));
    }
}
