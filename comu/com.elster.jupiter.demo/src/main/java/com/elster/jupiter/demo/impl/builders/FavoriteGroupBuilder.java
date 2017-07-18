/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;
import com.energyict.mdc.favorites.FavoritesService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class FavoriteGroupBuilder implements Builder<List<FavoriteDeviceGroup>> {
    private final FavoritesService favoritesService;
    private final UserService userService;

    private EndDeviceGroup group;

    @Inject
    public FavoriteGroupBuilder(FavoritesService favoritesService, UserService userService) {
        this.favoritesService = favoritesService;
        this.userService = userService;
    }

    public FavoriteGroupBuilder withGroup(EndDeviceGroup group){
        this.group = group;
        return this;
    }

    @Override
    public List<FavoriteDeviceGroup> get() {
        return create();
    }

    @Override
    public Optional<List<FavoriteDeviceGroup>> find() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public List<FavoriteDeviceGroup> create() {
        List<FavoriteDeviceGroup> groups = new ArrayList<>();
        userService.getUserQuery().select(Condition.TRUE).stream().forEach(user -> {
            groups.add(favoritesService.findOrCreateFavoriteDeviceGroup(group, user));
        });
        return groups;
    }
}
