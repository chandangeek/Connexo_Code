/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.favorites;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.Optional;

public interface FavoritesService {

    String COMPONENTNAME = "FAV";
    
    /*
     * Label categories
     */
    List<LabelCategory> getLabelCategories();
    
    Optional<LabelCategory> findLabelCategory(String name);
    
    LabelCategory createLabelCategory(String name);
    
    /*
     * Device labels
     */
    List<DeviceLabel> getDeviceLabels(Device device, User user);
    
    List<DeviceLabel> getDeviceLabelsOfCategory(User user, LabelCategory category);
    
    Optional<DeviceLabel> findDeviceLabel(Device device, User user, LabelCategory category);
    
    DeviceLabel findOrCreateDeviceLabel(Device device, User user, LabelCategory category, String comment);
    
    void removeDeviceLabel(DeviceLabel deviceLabel);
    
    /*
     * Favorite device groups
     */
    List<FavoriteDeviceGroup> getFavoriteDeviceGroups(User user);
    
    Optional<FavoriteDeviceGroup> findFavoriteDeviceGroup(EndDeviceGroup deviceGroup, User user);
    
    FavoriteDeviceGroup findOrCreateFavoriteDeviceGroup(EndDeviceGroup deviceGroup, User user);
    
    void removeFavoriteDeviceGroup(FavoriteDeviceGroup favouriteDeviceGroup);
    
}
