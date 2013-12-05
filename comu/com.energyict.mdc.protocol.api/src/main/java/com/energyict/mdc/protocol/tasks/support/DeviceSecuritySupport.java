package com.energyict.mdc.protocol.tasks.support;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;

/**
 * Defines the expected behavior for
 * {@link com.energyict.mdc.protocol.DeviceProtocol}s
 * that have security requirements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (16:10)
 */
public interface DeviceSecuritySupport extends DeviceProtocolSecurityCapabilities {

    /**
     * Setter for the {@link DeviceProtocolSecurityPropertySet}.
     * This groups the configured
     * {@link com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel} and
     * {@link com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel} with their
     * relevant configured properties.
     *
     * @param deviceProtocolSecurityPropertySet the {@link DeviceProtocolSecurityPropertySet}to set
     */
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

}