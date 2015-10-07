package com.energyict.mdc.device.data.impl.configchange;

/**
 * Object which serves as a 'business lock' when a DeviceConfigChange is currently happening
 */
public interface DeviceConfigChangeInAction {

    /**
     * Indication for self destruction.
     */
    void remove();
}
