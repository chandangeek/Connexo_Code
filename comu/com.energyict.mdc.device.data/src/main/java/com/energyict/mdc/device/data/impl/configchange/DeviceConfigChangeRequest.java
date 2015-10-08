package com.energyict.mdc.device.data.impl.configchange;

/**
 * Serves as a Business lock object for the action 'change device config'.
 * It works closely together with the {@link DeviceConfigChangeInAction} 'locks'.
 * These are created for each device in the 'change device config' action. Removing these
 * will trigger an eventhandler which checks if the request object can be removed.
 */
public interface DeviceConfigChangeRequest {

    /**
     * Indication for self destruction.
     */
    void remove();
}
