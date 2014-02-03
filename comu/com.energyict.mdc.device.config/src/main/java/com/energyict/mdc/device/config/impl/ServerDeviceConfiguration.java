package com.energyict.mdc.device.config.impl;

/**
 * Add behavior to {@link ServerDeviceConfiguration} that is
 * specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (15:28)
 */
public interface ServerDeviceConfiguration extends DeviceConfiguration {

    /**
     * Notifies this DeviceConfiguration that it is about to be deleted
     * as part of the delete of the {@link com.energyict.mdc.device.config.DeviceType}.
     */
    public void notifyDelete ();

}