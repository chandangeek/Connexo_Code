package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.tasks.support.DeviceSecuritySupport;

import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password and a (numerical)level to do authentication/encryption
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 15:10
 */
public class PasswordWithLevelSecuritySupport implements DeviceSecuritySupport {

    private final String translationKeyConstant = "PasswordWithLevelSecuritySupport.accesslevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.DEVICE_ACCESS_LEVEL.getPropertySpec(),
                DeviceSecurityProperty.PASSWORD.getPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.PASSWORD_AND_LEVEL.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.<AuthenticationDeviceAccessLevel>asList(new StandardAuthenticationAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.<EncryptionDeviceAccessLevel>asList(new StandardEncryptionAccessLevel());
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        for (PropertySpec securityProperty : getSecurityProperties()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    /**
     * Standard authentication level that requires a password and an access identifier
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.DEVICE_ACCESS_LEVEL.getPropertySpec(),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }

    /**
     * Standard encryption level that requires a password and an encryption identifier
     */
    protected class StandardEncryptionAccessLevel implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.DEVICE_ACCESS_LEVEL.getPropertySpec(),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }
}
