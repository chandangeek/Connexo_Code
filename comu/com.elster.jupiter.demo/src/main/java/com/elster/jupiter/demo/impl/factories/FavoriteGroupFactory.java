package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;
import com.energyict.mdc.favorites.FavoritesService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class FavoriteGroupFactory implements Factory<List<FavoriteDeviceGroup>> {
    private final FavoritesService favoritesService;
    private final UserService userService;

    private EndDeviceGroup group;

    @Inject
    public FavoriteGroupFactory(FavoritesService favoritesService, UserService userService) {
        this.favoritesService = favoritesService;
        this.userService = userService;
    }

    public FavoriteGroupFactory withGroup(EndDeviceGroup group){
        this.group = group;
        return this;
    }

    @Override
    public List<FavoriteDeviceGroup> get() {
        List<FavoriteDeviceGroup> groups = new ArrayList<>();
        userService.getUserQuery().select(Condition.TRUE).stream().forEach(user -> {
            groups.add(favoritesService.findOrCreateFavoriteDeviceGroup(group, user));
        });
        return groups;
    }
}
