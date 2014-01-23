package com.energyict.protocolimplv2.security;

import com.energyict.mdc.common.Password;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import java.util.Arrays;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password and a UserIdentification to do authentication/encryption.<br/>
 * Be aware that the UserIdentification is validated as a string, but can also just
 * contain a numerical value.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 9:28
 */
public class PasswordWithUserIdentificationSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private static final int STANDARD_AUTH_DEVICE_ACCESS_LEVEL = 10;
    private static final int STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL = 20;

    private PropertySpecService propertySpecService;

    public PasswordWithUserIdentificationSecuritySupport() {
        super();
    }

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService),
                DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.PASSWORD_AND_USER.toString();
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
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        overrideDeviceAccessIdentifierPropertyIfAbsent(typedProperties);

        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_AUTH_DEVICE_ACCESS_LEVEL, getAuthenticationAccessLevels()));
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL, getEncryptionAccessLevels()));
        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return STANDARD_AUTH_DEVICE_ACCESS_LEVEL;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return securityRelatedTypedProperties;
            }
        };
    }

    private void overrideDeviceAccessIdentifierPropertyIfAbsent(TypedProperties typedProperties) {
        Object deviceAccessIdentifier = typedProperties.getProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName());
        if(deviceAccessIdentifier == null){
            deviceAccessIdentifier =typedProperties.getProperty(MeterProtocol.NODEID);
        }
        if (deviceAccessIdentifier!=null) {
            typedProperties.setProperty(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService).getName(), deviceAccessIdentifier);
        }
    }

    /**
     * Standard authentication level that requires a password and an access identifier
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return STANDARD_AUTH_DEVICE_ACCESS_LEVEL;
        }

        @Override
        public String getTranslationKey() {
            return "PasswordWithUserIdentificationSecuritySupport.accesslevel.10";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

    /**
     * Standard encryption level that requires a password and an encryption identifier
     */
    protected class StandardEncryptionAccessLevel implements EncryptionDeviceAccessLevel {


        @Override
        public int getId() {
            return STANDARD_ENCRYPTION_DEVICE_ACCESS_LEVEL;
        }

        @Override
        public String getTranslationKey() {
            return "PasswordWithUserIdentificationSecuritySupport.accesslevel.20";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER.getPropertySpec(propertySpecService),
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
        }
    }

}