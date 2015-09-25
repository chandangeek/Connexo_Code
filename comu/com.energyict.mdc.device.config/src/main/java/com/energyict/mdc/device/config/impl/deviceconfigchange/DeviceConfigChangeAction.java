package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfiguration;

/**
 * Value object containing the result of a single action that needs to be performed when the
 * configuration of a Device will be changed.
 */
public class DeviceConfigChangeAction<T extends HasId> {

    private final DeviceConfiguration originDeviceConfig;
    private final DeviceConfiguration destinationDeviceConfig;
    private T originDataSource;
    private T destinationDataSource;
    private DeviceConfigChangeActionType actionType;

    public DeviceConfigChangeAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
        this.originDeviceConfig = originDeviceConfig;
        this.destinationDeviceConfig = destinationDeviceConfig;
    }

    public T getOrigin() {
        return originDataSource;
    }

    public T getDestination() {
        return destinationDataSource;
    }

    public DeviceConfigChangeActionType getActionType() {
        return actionType;
    }

    public void setOrigin(T originDataSource) {
        this.originDataSource = originDataSource;
    }

    public void setDestination(T destinationDataSource) {
        this.destinationDataSource = destinationDataSource;
    }

    public void setActionType(DeviceConfigChangeActionType actionType) {
        this.actionType = actionType;
    }

    public DeviceConfiguration getOriginDeviceConfiguration(){
        return originDeviceConfig;
    }

    public DeviceConfiguration getDestinationDeviceConfiguration() {
        return destinationDeviceConfig;
    }
}
