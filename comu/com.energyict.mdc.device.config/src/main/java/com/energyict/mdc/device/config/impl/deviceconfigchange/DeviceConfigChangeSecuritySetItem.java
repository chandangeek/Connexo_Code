package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;

import java.util.List;
import java.util.function.Predicate;

/**
 * Serves as a helper item to calculate the difference in SecurityPropertySets for the foreseen DeviceConfigurations
 */
public class DeviceConfigChangeSecuritySetItem implements DeviceConfigChangeItem<SecurityPropertySet> {

    private final DeviceConfiguration originDeviceConfig;
    private final DeviceConfiguration destinationDeviceConfig;

    public DeviceConfigChangeSecuritySetItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
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

    @Override
    public List<SecurityPropertySet> getOriginItems() {
        return originDeviceConfig.getSecurityPropertySets();
    }

    @Override
    public List<SecurityPropertySet> getDestinationItems() {
        return destinationDeviceConfig.getSecurityPropertySets();
    }

    @Override
    public Predicate<SecurityPropertySet> exactSameItem(SecurityPropertySet item) {
        return securityPropertySet -> securityPropertySet.getName().equals(item.getName())
                && securityPropertySet.getAuthenticationDeviceAccessLevel().equals(item.getAuthenticationDeviceAccessLevel())
                && securityPropertySet.getEncryptionDeviceAccessLevel().equals(item.getEncryptionDeviceAccessLevel());
    }

    @Override
    public Predicate<SecurityPropertySet> isItAConflict(SecurityPropertySet item) {
        return securityPropertySet -> !securityPropertySet.getName().equals(item.getName())
                && securityPropertySet.getAuthenticationDeviceAccessLevel().equals(item.getAuthenticationDeviceAccessLevel())
                && securityPropertySet.getEncryptionDeviceAccessLevel().equals(item.getEncryptionDeviceAccessLevel());    }
}
