package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.*;
import com.energyict.mdc.protocol.tasks.support.DeviceSecuritySupport;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 10:42
 * Author: khe
 */
public class NoSecuritySupport implements DeviceSecuritySupport {

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.NONE.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return null;
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        //This instance doesn't have any security or authentication
    }
}
