/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet;


import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import com.energyict.protocolimplv2.security.DeviceSecurityProperty;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private enum AuthenticationAccessLevelIds {
        NO_AUTHENTICATION(0);

        private final int accessLevel;

        AuthenticationAccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        int getAccessLevel() {
            return accessLevel;
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

        int getAccessLevel() {
            return accessLevel;
        }
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new GarnetSecuritySupportCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList((AuthenticationDeviceAccessLevel) new NoAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.singletonList((EncryptionDeviceAccessLevel) new MessageEncryption());
    }

    /**
     * An encryption level where the data of the frame is encrypted using
     * the manufacturer or the customer key
     */
    protected class MessageEncryption implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return EncryptionAccessLevelIds.MESSAGE_ENCRYPTION.getAccessLevel();
        }

        @Override
        public String getTranslation() {
            return thesaurus.getFormat(TranslationKeys.GARNET_ENCRYPTION_LEVEL_1).format();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.CUSTOMER_ENCRYPTION_KEY.getPropertySpec(propertySpecService, thesaurus),
                    DeviceSecurityProperty.MANUFACTURER_ENCRYPTION_KEY.getPropertySpec(propertySpecService, thesaurus));
        }
    }

    /**
     * An authentication level which indicate that no authentication is required
     * for communication with the device.
     */
    protected class NoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AuthenticationAccessLevelIds.NO_AUTHENTICATION.getAccessLevel();
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