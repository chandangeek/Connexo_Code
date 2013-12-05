package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;

/**
 * Defines the expected behavior for
 * {@link DeviceProtocol}s
 * that have security requirements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-13 (16:10)
 */
public interface DeviceSecuritySupport extends DeviceProtocolSecurityCapabilities {

    /**
     * Setter for the {@link DeviceProtocolSecurityPropertySet}.
     * This groups the configured
     * {@link AuthenticationDeviceAccessLevel} and
     * {@link EncryptionDeviceAccessLevel} with their
     * relevant configured properties.
     *
     * @param deviceProtocolSecurityPropertySet the {@link DeviceProtocolSecurityPropertySet}to set
     */
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

}