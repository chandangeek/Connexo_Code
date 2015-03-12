package com.energyict.mdc.device.lifecycle.config;

/**
 * Provides building services for {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (16:31)
 */
public interface DeviceLifeCycleBuilder {

    /**
     * Gets the {@link DeviceLifeCycle} that was being constructed with this builder.
     * Note that it is your responsibility to save the DeviceLifeCycle.
     *
     * @return The DeviceLifeCycle
     * @see DeviceLifeCycle#save()
     */
    public DeviceLifeCycle complete();

}