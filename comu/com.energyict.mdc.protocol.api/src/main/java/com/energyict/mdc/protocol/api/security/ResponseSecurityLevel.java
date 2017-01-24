package com.energyict.mdc.protocol.api.security;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * <p/>
 * Protocols that have advanced security capabilities can specify here
 * exactly which security applies to the responses received from a device.
 * <p/>
 * Note that the usage of this level replaces the usage of {@link EncryptionDeviceAccessLevel}
 *
 * @author khe
 * @since 5/02/2016 - 17:41
 */
public interface ResponseSecurityLevel extends DeviceAccessLevel, Serializable {
}
