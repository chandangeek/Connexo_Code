package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;

import java.util.Collections;
import java.util.List;

/**
 * If a protocol has this access level in its list of supported access levels,
 * it means that it's a slave device and it can inherit the security properties of its master device.
 * <p/>
 * Copyrights EnergyICT
 * Author: khe
 */
public class InheritedSecuritySuite implements SecuritySuite {

    public static final String TRANSLATION_KEY = "inheritedDeviceAccessLevel";

    @Override
    public int getId() {
        return DeviceAccessLevel.CAN_INHERIT_PROPERTIES_FROM_MASTER_ID;
    }

    @Override
    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    @Override
    public String getDefaultTranslation() {
        return "Inherited from master device";
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return Collections.emptyList();
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return Collections.emptyList();
    }
}