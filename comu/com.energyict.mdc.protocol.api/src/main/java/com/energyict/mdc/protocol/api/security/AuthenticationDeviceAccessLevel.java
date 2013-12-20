package com.energyict.mdc.protocol.api.security;

import com.energyict.mdc.dynamic.PropertySpec;

/**
 * Models a level of security for a physical device
 * and the {@link PropertySpec}s
 * that the Device will require to be specified
 * to authenticate access the data that is
 * secured by this level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (16:54)
 */
public interface AuthenticationDeviceAccessLevel extends DeviceAccessLevel {
}