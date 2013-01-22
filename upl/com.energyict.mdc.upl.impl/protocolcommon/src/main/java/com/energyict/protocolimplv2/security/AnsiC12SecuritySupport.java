package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for an Ansi C12 protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 12:02
 */
public class AnsiC12SecuritySupport implements DeviceProtocolSecurityCapabilities {

    private final String authenticationTranslationKeyConstant = "AnsiC12SecuritySupport.authenticationlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.ANSI_C12_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new UnRestrictedAuthentication(),
                new RestrictedAuthentication(),
                new ReadOnlyAuthentication()
        );
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
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
     * UnRestricted ANSI C12 authentication level
     */
    protected class UnRestrictedAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
            );
        }
    }

    /**
     * Restricted ANSI C12 authentication level
     */
    protected class RestrictedAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
            );
        }
    }

    /**
     * Read Only ANSI C12 authentication level
     */
    protected class ReadOnlyAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 2;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER.getPropertySpec(),
                    DeviceSecurityProperty.ANSI_C12_USER_ID.getPropertySpec()
            );
        }
    }
}
