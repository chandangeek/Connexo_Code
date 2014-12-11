package com.energyict.protocolimplv2.security;

import com.energyict.mdc.common.Password;
import com.energyict.mdc.common.TypedProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for an IEC1107 protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 11:10
 */
public class IEC1107SecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    public static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String DEFAULT_SECURITY_LEVEL_VALUE = "1";
    private static final String translationKeyConstant = "IEC1107SecuritySupport.authenticationlevel.";

    private final PropertySpecService propertySpecService;

    @Inject
    public IEC1107SecuritySupport(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    /**
     * Summarizes the used ID for the AuthenticationLevels.
     */
    protected enum AccessLevelIds {
        NO_AUTHENTICATION(0),
        LEVEL_ONE(1),
        LEVEL_TWO(2),
        LEVEL_THREE(3);

        private final int accessLevel;

        private AccessLevelIds(int accessLevel) {
            this.accessLevel = accessLevel;
        }

        private int getAccessLevel() {
            return this.accessLevel;
        }

    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService));
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.IEC1107_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(
                new NoAuthentication(),
                new LevelOneAuthentication(),
                new LevelTwoAuthentication(),
                new LevelThreeAuthentication());
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

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            // override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), ((Password) property).getValue());
            } else {
                typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(), property);
            }
            typedProperties.setProperty(SECURITY_LEVEL_PROPERTY_NAME, String.valueOf(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        String securityLevelProperty = typedProperties.getTypedProperty(SECURITY_LEVEL_PROPERTY_NAME, DEFAULT_SECURITY_LEVEL_VALUE);
        final int authenticationLevel = Integer.valueOf(securityLevelProperty);
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, authenticationLevel, getAuthenticationAccessLevels()));
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return authenticationLevel;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    /**
     * An authentication level which indicate that no authentication is required
     * for communication with the device.
     */
    protected class NoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.NO_AUTHENTICATION.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    /**
     * Authentication for level 1 security
     */
    protected class LevelOneAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_ONE.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    /**
     * Authentication for level 2 security
     */
    protected class LevelTwoAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_TWO.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    /**
     * Authentication for level 1 security
     */
    protected class LevelThreeAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return AccessLevelIds.LEVEL_THREE.accessLevel;
        }

        @Override
        public String getTranslationKey() {
            return translationKeyConstant + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

}