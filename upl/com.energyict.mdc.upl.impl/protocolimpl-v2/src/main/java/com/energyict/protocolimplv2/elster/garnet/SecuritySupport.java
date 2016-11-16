package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.cpo.PropertySpec;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;
import com.energyict.protocolimplv2.security.SecurityRelationTypeName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 18/06/2014 - 10:54
 */
class SecuritySupport implements DeviceProtocolSecurityCapabilities {

    private static final String authenticationTranslationKeyConstant = "GarnetSecuritySupport.authenticationlevel.0";
    private static final String encryptionTranslationKeyConstant = "GarnetSecuritySupport.encryptionlevel.1";

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    private enum AuthenticationAccessLevelIds {
        NO_AUTHENTICATION(0);

        private final int accessLevel;

        AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        private int getAccessLevel() {
            return this.accessLevel;
        }
    }

    /**
     * Summarizes the used ID for the EncryptionLevels.
     */
    private enum EncryptionAccessLevelIds {
        MESSAGE_ENCRYPTION(1);

        private final int accessLevel;

        EncryptionAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        private int getAccessLevel() {
            return this.accessLevel;
        }
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(),
                DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec()
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.GARNET_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList((AuthenticationDeviceAccessLevel) new NoAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.singletonList((EncryptionDeviceAccessLevel) new MessageEncryption());
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
     * An encryption level where the data of the frame is encrypted using
     * the manufacturer or the customer key
     */
    private class MessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(),
                    DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec());
        }
    }

    /**
     * An authentication level which indicate that no authentication is required
     * for communication with the device.
     */
    private class NoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant;
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }
}