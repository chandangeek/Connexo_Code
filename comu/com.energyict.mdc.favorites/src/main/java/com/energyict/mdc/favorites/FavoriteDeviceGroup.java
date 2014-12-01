package com.energyict.mdc.favorites;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.users.User;

public interface FavoriteDeviceGroup {

    EndDeviceGroup getEndDeviceGroup();
    
    User getUser();
    
}
