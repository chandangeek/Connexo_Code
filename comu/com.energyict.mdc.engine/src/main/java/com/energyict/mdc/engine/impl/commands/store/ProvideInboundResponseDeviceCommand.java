package com.energyict.mdc.engine.impl.commands.store;

/**
 * A DeviceCommand that during it's execute method provides a proper response to the actual Device.
 */
public interface ProvideInboundResponseDeviceCommand extends DeviceCommand {

    void dataStorageFailed();
}
