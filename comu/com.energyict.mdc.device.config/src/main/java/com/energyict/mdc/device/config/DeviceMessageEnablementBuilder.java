package com.energyict.mdc.device.config;

/**
 * Copyrights EnergyICT
 * Date: 10/7/14
 * Time: 2:29 PM
 */
public interface DeviceMessageEnablementBuilder {

    DeviceMessageEnablementBuilder addUserAction(DeviceMessageUserAction deviceMessageUserAction);

    DeviceMessageEnablement build();

}
