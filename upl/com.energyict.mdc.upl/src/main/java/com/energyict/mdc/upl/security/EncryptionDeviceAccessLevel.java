package com.energyict.mdc.upl.security;

import java.io.Serializable;

/**
 * Models a level of security for a Device
 * and the {@link com.energyict.mdc.upl.properties.PropertySpec}s
 * that the Device will require to be specified
 * before decrypting the data that is
 * secured by this level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (17:17)
 */
public interface EncryptionDeviceAccessLevel extends DeviceAccessLevel, Serializable {
}