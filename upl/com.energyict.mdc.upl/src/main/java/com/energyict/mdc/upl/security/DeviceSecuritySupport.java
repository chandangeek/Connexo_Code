package com.energyict.mdc.upl.security;

/**
 * Defines the expected behavior for
 * {@link com.energyict.mdc.upl.DeviceProtocol}s
 * that have security requirements.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-16 (09:55)
 */
public interface DeviceSecuritySupport extends DeviceProtocolSecurityCapabilities {

    /**
     * Setter for the {@link DeviceProtocolSecurityPropertySet}.
     * This groups the configured {@link AuthenticationDeviceAccessLevel} and
     * {@link EncryptionDeviceAccessLevel} with their relevant configured properties.
     *
     * @param deviceProtocolSecurityPropertySet the {@link DeviceProtocolSecurityPropertySet}
     */
    void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

}