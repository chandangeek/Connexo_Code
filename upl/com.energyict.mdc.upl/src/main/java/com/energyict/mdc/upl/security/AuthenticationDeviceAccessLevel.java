package com.energyict.mdc.upl.security;

import java.io.Serializable;

/**
 * Models a level of security for a Device
 * and the {@link com.energyict.mdc.upl.properties.PropertySpec}s
 * that the Device will require to be specified
 * to authenticate access the data that is
 * secured by this level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (16:54)
 */
public interface AuthenticationDeviceAccessLevel extends DeviceAccessLevel, Serializable {
}