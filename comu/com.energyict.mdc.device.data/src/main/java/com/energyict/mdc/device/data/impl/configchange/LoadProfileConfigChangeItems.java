/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.DeviceConfigChangeActionType;
import com.energyict.mdc.device.config.DeviceConfigChangeEngine;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.LoadProfile;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Applies changes to the loadProfiles of the Device
 */
public class LoadProfileConfigChangeItems extends AbstractConfigChangeItem {

    private static LoadProfileConfigChangeItems INSTANCE = new LoadProfileConfigChangeItems();

    private LoadProfileConfigChangeItems() {
    }

    static DataSourceConfigChangeItem getInstance() {
        return INSTANCE;
    }

    @Override
    public void apply(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        final List<DeviceConfigChangeAction<LoadProfileSpec>> loadProfileActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsFor(new DeviceConfigChangeLoadProfileItem(originDeviceConfiguration, destinationDeviceConfiguration));

        final List<LoadProfileSpec> loadProfileSpecsToAdd = getAddItems(loadProfileActions);
        final List<DeviceConfigChangeAction<LoadProfileSpec>> matchedLoadProfileSpecs = getMatchItems(loadProfileActions);
        final List<LoadProfile> loadProfilesToRemove = loadProfileActions.stream()
                .filter(actionTypeIs(DeviceConfigChangeActionType.REMOVE))
                .flatMap(loadProfileSpecDeviceConfigChangeAction1 -> device.getLoadProfiles().stream()
                        .filter(onCorrespondingLoadProfile(loadProfileSpecDeviceConfigChangeAction1)))
                .collect(Collectors.toList());

        device.removeLoadProfiles(loadProfilesToRemove);
        device.addLoadProfiles(loadProfileSpecsToAdd);
        matchedLoadProfileSpecs.stream().forEach(loadProfileSpecDeviceConfigChangeAction -> device.getLoadProfiles().stream()
                .filter(onCorrespondingLoadProfile(loadProfileSpecDeviceConfigChangeAction))
                .findFirst()
                .ifPresent(loadProfile -> ((ServerLoadProfileForConfigChange) loadProfile).setNewLoadProfileSpec(loadProfileSpecDeviceConfigChangeAction.getDestination())));
    }

    private Predicate<LoadProfile> onCorrespondingLoadProfile(DeviceConfigChangeAction<LoadProfileSpec> loadProfileSpecDeviceConfigChangeAction) {
        return loadProfile -> loadProfile.getLoadProfileSpec().getId() == loadProfileSpecDeviceConfigChangeAction.getOrigin().getId();
    }

    private class DeviceConfigChangeLoadProfileItem extends AbstractDeviceConfigChangeItem<LoadProfileSpec> {

        DeviceConfigChangeLoadProfileItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
            super(originDeviceConfig, destinationDeviceConfig);
        }

        @Override
        public List<LoadProfileSpec> getOriginItems() {
            return originDeviceConfig.getLoadProfileSpecs();
        }

        @Override
        public List<LoadProfileSpec> getDestinationItems() {
            return destinationDeviceConfig.getLoadProfileSpecs();
        }

        @Override
        public Predicate<LoadProfileSpec> exactSameItem(LoadProfileSpec item) {
            return loadProfileSpec -> loadProfileSpec.getLoadProfileType().getId() == item.getLoadProfileType().getId();
        }

        @Override
        public Predicate<LoadProfileSpec> isItAConflict(LoadProfileSpec item) {
            return loadProfileSpec -> false; // no conflicts possible (for now)
        }
    }

}
