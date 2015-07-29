package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Models the link between {@link DeviceTypeImpl} and {@link DeviceLifeCycle} over time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (08:41)
 */
public interface DeviceLifeCycleInDeviceType extends Effectivity {

    public DeviceLifeCycle getDeviceLifeCycle();

    public DeviceTypeImpl getDeviceType();

    public Optional<User> getUser();

    /**
     * Closes this DeviceLifeCycleInDeviceType so that it is
     * only effective until the specified closing Date.
     *
     * @param closingDate The closing Date
     */
    public void close(Instant closingDate);

}