package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.tasks.support.DeviceSecuritySupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password to do authentication/encryption
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 14:47
 */
public class SimplePasswordSecuritySupport implements DeviceSecuritySupport {

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.SIMPLE_PASSWORD.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.<AuthenticationDeviceAccessLevel>asList(new SimpleAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        if (DeviceSecurityProperty.PASSWORD.getPropertySpec().getName().equals(name)) {
            return DeviceSecurityProperty.PASSWORD.getPropertySpec();
        } else {
            return null;
        }
    }

    /**
     * A simple authentication level that requires a single password
     */
    protected class SimpleAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 10;
        }

        @Override
        public String getTranslationKey() {
            return "SimplePasswordSecuritySupport.accesslevel.10";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }
}
