package com.energyict.mdc.protocol.api.security;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * <p/>
 * Protocols that have advanced security capabilities can specify here
 * exactly which security applies to the requests that the ComServer sends to the devices.
 * <p/>
 * Note that the usage of this level replaces the usage of {@link EncryptionDeviceAccessLevel}
 *
 * @author khe
 * @since 5/02/2016 - 17:41
 */
public interface RequestSecurityLevel extends DeviceAccessLevel, Serializable {
}
