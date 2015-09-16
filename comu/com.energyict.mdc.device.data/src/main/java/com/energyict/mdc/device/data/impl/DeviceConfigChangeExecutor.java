package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfigChangeEngine;
import com.energyict.mdc.device.config.DeviceConfigChangeItem;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeActionType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Applies the actual change in DeviceConfiguration for a single Device
 */
public final class DeviceConfigChangeExecutor {

    private static DeviceConfigChangeExecutor ourInstance = new DeviceConfigChangeExecutor();

    public static DeviceConfigChangeExecutor getInstance() {
        return ourInstance;
    }

    private DeviceConfigChangeExecutor() {
    }

    public Device execute(ServerDeviceForConfigChange device, DeviceConfiguration deviceConfiguration) {
        final DeviceConfiguration originDeviceConfiguration = device.getDeviceConfiguration();
        //TODO check to lock here or somewhere else
        device.lock();
        device.createNewMeterActivation();
        device.setNewDeviceConfiguration(deviceConfiguration);

        final List<DeviceConfigChangeAction<LoadProfileSpec>> loadProfileActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsFor(new DeviceConfigChangeLoadProfileItem(originDeviceConfiguration, deviceConfiguration));
        final List<LoadProfileSpec> loadProfileSpecsToAdd = loadProfileActions.stream().filter(loadProfileSpecDeviceConfigChangeAction -> loadProfileSpecDeviceConfigChangeAction.getActionType().equals(DeviceConfigChangeActionType.ADD)).map(DeviceConfigChangeAction::getDestination).collect(Collectors.toList());
        final List<LoadProfile> loadProfilesToRemove = loadProfileActions.stream().filter(loadProfileSpecDeviceConfigChangeAction -> loadProfileSpecDeviceConfigChangeAction.getActionType().equals(DeviceConfigChangeActionType.REMOVE)).flatMap(loadProfileSpecDeviceConfigChangeAction1 -> device.getLoadProfiles().stream().filter(loadProfile -> loadProfile.getLoadProfileSpec().getId() == loadProfileSpecDeviceConfigChangeAction1.getOrigin().getId())).collect(Collectors.toList());
        final List<DeviceConfigChangeAction<LoadProfileSpec>> matches = loadProfileActions.stream().filter(loadProfileSpecDeviceConfigChangeAction -> loadProfileSpecDeviceConfigChangeAction.getActionType().equals(DeviceConfigChangeActionType.MATCH)).collect(Collectors.toList());

        device.removeLoadProfiles(loadProfilesToRemove);
        device.addLoadProfiles(loadProfileSpecsToAdd);
        matches.stream().forEach(loadProfileSpecDeviceConfigChangeAction -> device.getLoadProfiles().stream()
                .filter(loadProfile ->
                        loadProfile.getLoadProfileSpec().getId() == loadProfileSpecDeviceConfigChangeAction.getOrigin().getId())
                .findFirst()
                .ifPresent(loadProfile1 ->
                        ((ServerLoadProfileForConfigChange) loadProfile1).setNewLoadProfileSpec(loadProfileSpecDeviceConfigChangeAction.getDestination())));

        device.save();
        return device;
    }

    abstract class AbstractDeviceConfigChangeItem<T extends HasId> implements DeviceConfigChangeItem<T> {

        final DeviceConfiguration originDeviceConfig;
        final DeviceConfiguration destinationDeviceConfig;

        public AbstractDeviceConfigChangeItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
            this.originDeviceConfig = originDeviceConfig;
            this.destinationDeviceConfig = destinationDeviceConfig;
        }

        @Override
        public DeviceConfiguration getOriginDeviceConfig() {
            return originDeviceConfig;
        }

        @Override
        public DeviceConfiguration getDestinationDeviceConfig() {
            return destinationDeviceConfig;
        }
    }

    class DeviceConfigChangeLoadProfileItem extends AbstractDeviceConfigChangeItem<LoadProfileSpec> {

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
            return loadProfileSpec -> false;
        }
    }
}
