package com.energyict.protocolimplv2.elster.garnet;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocolimplv2.security.DeviceSecurityProperty;
import com.energyict.protocolimplv2.security.SecurityRelationTypeName;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author sva
 * @since 18/06/2014 - 10:54
 */
public class SecuritySupport implements DeviceProtocolSecurityCapabilities {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    public SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    protected enum AuthenticationAccessLevelIds {
        NO_AUTHENTICATION(0);

        private final int accessLevel;

        private AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

    }

    /**
     * Summarizes the used ID for the EncryptionLevels.
     */
    protected enum EncryptionAccessLevelIds {
        MESSAGE_ENCRYPTION(1);

        private final int accessLevel;

        private EncryptionAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return Arrays.asList(
                DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec(propertySpecService)
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.GARNET_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList((AuthenticationDeviceAccessLevel) new NoAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.asList((EncryptionDeviceAccessLevel) new MessageEncryption());
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        for (PropertySpec securityProperty : getSecurityPropertySpecs()) {
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
    protected class MessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.GARNET_ENCRYPTION_LEVEL_1).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec(propertySpecService));
        }
    }

    /**
     * An authentication level which indicate that no authentication is required
     * for communication with the device.
     */
    protected class NoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.GARNET_AUTHENTICATION_LEVEL_0).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }
}