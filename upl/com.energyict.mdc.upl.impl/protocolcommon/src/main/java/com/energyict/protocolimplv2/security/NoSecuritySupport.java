package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 10:42
 * Author: khe
 */
public class NoSecuritySupport extends AbstractSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final int NO_SECURITY_SUPPORT_ID = 0;
    private static final String authenticationTranslationKeyConstant = "NoSecuritySupport.authenticationlevel.";

    public NoSecuritySupport() {
        super(null);        //No property specs here
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return Optional.empty();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.singletonList(new NoAuthenticationAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return null;
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        return TypedProperties.empty();
    }

    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public String getName() {
                return "security";
            }

            @Override
            public Object getClient() {
                return null;
            }

            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return NO_SECURITY_SUPPORT_ID;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return TypedProperties.empty();
            }
        };
    }

    /**
     * No authentication level that requires nothing
     */
    private class NoAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return NO_SECURITY_SUPPORT_ID;
        }

        @Override
        public String getTranslationKey() {
            return authenticationTranslationKeyConstant + getId();
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