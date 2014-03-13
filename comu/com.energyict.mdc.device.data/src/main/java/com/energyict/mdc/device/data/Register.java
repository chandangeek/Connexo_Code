package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.BaseRegister;

/**
 * Defines the non-persistent representation of a Register.
 * A <i>Register</i> for a {@link Device} will be a wrapper around
 * the {@link com.energyict.mdc.device.config.RegisterSpec} of
 * the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * of the {@link Device}
 *
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 10:32
 */
public interface Register extends BaseRegister{

    Device getDevice();

    /**
     * Returns the register's specification object
     *
     * @return the spec
     */
    RegisterSpec getRegisterSpec();
}
