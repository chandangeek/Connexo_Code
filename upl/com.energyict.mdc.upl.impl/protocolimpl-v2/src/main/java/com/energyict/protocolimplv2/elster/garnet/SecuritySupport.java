package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 18/06/2014 - 10:54
 */
class SecuritySupport {

    private static final String authenticationTranslationKeyConstant = "GarnetSecuritySupport.authenticationlevel.0";
    private static final String encryptionTranslationKeyConstant = "GarnetSecuritySupport.encryptionlevel.1";

    public List<PropertySpec> getSecurityProperties(PropertySpecService propertySpecService) {
        return Arrays.asList(
                DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec(propertySpecService)
        );
    }

    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList((AuthenticationDeviceAccessLevel) new NoAuthentication());
    }

    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels(PropertySpecService propertySpecService) {
        return Collections.singletonList((EncryptionDeviceAccessLevel) new MessageEncryption(propertySpecService));
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    private enum AuthenticationAccessLevelIds {
        NO_AUTHENTICATION(0);

        private final int accessLevel;

        AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
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
    }

    /**
     * An encryption level where the data of the frame is encrypted using
     * the manufacturer or the customer key
     */
    private static class MessageEncryption implements EncryptionDeviceAccessLevel {
        private final PropertySpecService propertySpecService;

        private MessageEncryption(PropertySpecService propertySpecService) {
            this.propertySpecService = propertySpecService;
        }

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return encryptionTranslationKeyConstant;
        }

        @Override
        public String getDefaultTranslation() {
            return "Message encryption";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(this.propertySpecService),
                    DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec(this.propertySpecService));
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
        public String getDefaultTranslation() {
            return "No authentication";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }
}