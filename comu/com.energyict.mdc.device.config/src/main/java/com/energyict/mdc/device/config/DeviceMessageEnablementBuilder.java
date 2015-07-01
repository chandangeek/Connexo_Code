package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;

/**
 * Copyrights EnergyICT
 * Date: 10/7/14
 * Time: 2:29 PM
 */
@ProviderType
public interface DeviceMessageEnablementBuilder {

    /**
     * Adds a single DeviceMessageUserAction to the builder
     *
     * @param deviceMessageUserAction the deviceMessageUserAction to add
     * @return the currently building builder
     */
    DeviceMessageEnablementBuilder addUserAction(DeviceMessageUserAction deviceMessageUserAction);

    /**
     * Adds multiple DeviceMessageUserActions to the builder
     *
     * @param deviceMessageUserActions the deviceMessageUserActions to add
     * @return the currently building builder
     */
    DeviceMessageEnablementBuilder addUserActions(DeviceMessageUserAction... deviceMessageUserActions);

    DeviceMessageEnablement build();

}
