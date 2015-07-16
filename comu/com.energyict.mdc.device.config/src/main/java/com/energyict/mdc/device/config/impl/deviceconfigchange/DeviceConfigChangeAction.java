package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.DeviceConfiguration;

/**
 * Value object containing the result a single action that needs to be performed when the
 * configuration of a Device will be changed
 */
public class DeviceConfigChangeAction<T extends HasId> {

    private final DeviceConfiguration originDeviceConfig;
    private final DeviceConfiguration destinationDeviceConfig;
    private T originConnectionTask;
    private T destinationConnectionTask;
    private DeviceConfigChangeActionType actionType;
    private ConflictingConnectionMethodSolution solution;

    public DeviceConfigChangeAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
        this.originDeviceConfig = originDeviceConfig;
        this.destinationDeviceConfig = destinationDeviceConfig;
    }

    public T getOrigin() {
        return originConnectionTask;
    }

    public T getDestination() {
        return destinationConnectionTask;
    }

    public DeviceConfigChangeActionType getActionType() {
        return actionType;
    }

    public ConflictingConnectionMethodSolution getSolution() {
        return solution;
    }

    public void setSolution(ConflictingConnectionMethodSolution solution) {
        this.solution = solution;
    }

    public void setOrigin(T originConnectionTask) {
        this.originConnectionTask = originConnectionTask;
    }

    public void setDestination(T destinationConnectionTask) {
        this.destinationConnectionTask = destinationConnectionTask;
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
