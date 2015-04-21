package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

/**
 * Models the implementation behavior of the {@link MicroAction}
 * interface and is therefore reserverd for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (09:58)
 */
public interface ServerMicroAction {

    /**
     * Executes this Action on the specified {@link Device}.
     *
     * @param device The Device
     */
    public void execute(Device device);

}