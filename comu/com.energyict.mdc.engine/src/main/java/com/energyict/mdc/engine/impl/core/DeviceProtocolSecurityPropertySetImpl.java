/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;

public class DeviceProtocolSecurityPropertySetImpl implements DeviceProtocolSecurityPropertySet {

    private int authenticationDeviceAccessLevel;
    private int encryptionDeviceAccessLevel;
    private TypedProperties securityProperties;

    public DeviceProtocolSecurityPropertySetImpl(int authenticationDeviceAccessLevel, int encryptionDeviceAccessLevel, TypedProperties securityProperties) {
        this.authenticationDeviceAccessLevel = authenticationDeviceAccessLevel;
        this.encryptionDeviceAccessLevel = encryptionDeviceAccessLevel;
        this.securityProperties = securityProperties;
    }

    @Override
    public int getAuthenticationDeviceAccessLevel() {
        return authenticationDeviceAccessLevel;
    }

    @Override
    public int getEncryptionDeviceAccessLevel() {
        return encryptionDeviceAccessLevel;
    }

    @Override
    public TypedProperties getSecurityProperties() {
        return securityProperties;
    }

}