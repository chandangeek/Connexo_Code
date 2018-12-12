/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.favorites.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;

public class FavoriteDeviceGroupImpl implements FavoriteDeviceGroup {

    @IsPresent(message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<User> user = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    FavoriteDeviceGroupImpl() {
        super();
    }

    FavoriteDeviceGroupImpl(EndDeviceGroup endDeviceGroup, User user) {
        this();
        setEndDeviceGroup(endDeviceGroup);
        setUser(user);
    }

    @Override
    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }
}
