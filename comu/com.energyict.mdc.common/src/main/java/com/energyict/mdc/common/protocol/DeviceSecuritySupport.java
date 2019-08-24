/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import com.energyict.mdc.common.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

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
    void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

}