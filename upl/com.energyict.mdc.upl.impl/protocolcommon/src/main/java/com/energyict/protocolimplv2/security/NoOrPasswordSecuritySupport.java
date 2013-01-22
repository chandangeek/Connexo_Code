package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;

import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for device that has either:
 * <ul><li>No password</li>
 * <li>A password</li></ul>
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 14:41
 */
public class NoOrPasswordSecuritySupport implements DeviceProtocolSecurityCapabilities {

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
