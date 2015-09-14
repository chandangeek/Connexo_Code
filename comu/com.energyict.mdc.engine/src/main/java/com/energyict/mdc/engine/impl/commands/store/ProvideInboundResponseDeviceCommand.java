package com.energyict.mdc.engine.impl.commands.store;

/**
 * A DeviceCommand that during it's execute method provides a proper response to the actual Device.
 */
public interface ProvideInboundResponseDeviceCommand extends DeviceCommand {

    /**
     * Indicate that the storage of the data was not performed
     */
    void dataStorageFailed();

}
