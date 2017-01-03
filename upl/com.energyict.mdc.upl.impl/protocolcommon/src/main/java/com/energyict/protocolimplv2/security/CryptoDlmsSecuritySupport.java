package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacyDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import com.energyict.protocolimpl.properties.TypedProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Special security support implementation: this one has no real auth/encr levels.
 * The only level 'definedByProperty' is a place holder.
 * <p/>
 * The SecurityLevel property is added to the list of required security properties, this one can easily be changed per device in the UI.
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/03/2015 - 11:49
 */
public class CryptoDlmsSecuritySupport implements LegacyDeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    /* Legacy property names*/
    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";
    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String HLS_SECRET_LEGACY_PROPERTY_NAME = "HlsSecret";
    private static final String HEX_PASSWORD_LEGACY_PROPERTY_NAME = "HexPassword";
    private static final String CRYPTOSERVER_LEGACY_PROPERTY_NAME = "CryptoServer";

    private final PropertySpecService propertySpecService;

    public CryptoDlmsSecuritySupport(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME,
                DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME,
                HLS_SECRET_LEGACY_PROPERTY_NAME,
                HEX_PASSWORD_LEGACY_PROPERTY_NAME,
                CRYPTOSERVER_LEGACY_PROPERTY_NAME);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.CRYPTOSERVER_PHASE.getPropertySpec(this.propertySpecService),
                DeviceSecurityProperty.SECURITY_LEVEL.getPropertySpec(this.propertySpecService)
        );
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> levels = new ArrayList<>();
        levels.add(new DefinedByProperty());
        return levels;
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        List<EncryptionDeviceAccessLevel> levels = new ArrayList<>();
        levels.add(new DefinedByProperty());
        return levels;
    }

    @Override
    public com.energyict.mdc.upl.properties.TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());    //Adds ClientMacAddress and SecurityLevel, their names have not changed

            // HlsSecret: override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), new EmptyPassword());
            if (Password.class.isAssignableFrom(property.getClass())) {
                typedProperties.setProperty(HLS_SECRET_LEGACY_PROPERTY_NAME, ((Password) property).getValue());
            } else {
                typedProperties.setProperty(HLS_SECRET_LEGACY_PROPERTY_NAME, property);
            }

            //AK
            typedProperties.setProperty(DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), ""));

            //EK
            typedProperties.setProperty(DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME,
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), ""));

            //CryptoServer phase property
            typedProperties.setProperty(CRYPTOSERVER_LEGACY_PROPERTY_NAME, deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.CRYPTOSERVER_PHASE.toString(), ""));
        }
        return typedProperties;
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(com.energyict.mdc.upl.properties.TypedProperties typedProperties) {
        return this.convertFromTypedProperties(TypedProperties.copyOf(typedProperties));
    }

    private DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties oldTypedProperties) {
        checkForCorrectClientMacAddressPropertySpecType(oldTypedProperties);
        final TypedProperties result = TypedProperties.empty();
        result.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(oldTypedProperties, 0, getAuthenticationAccessLevels()));

        //Add properties that have a new key name or format (compared to EIServer 8.x)
        if (oldTypedProperties.hasValueFor(HLS_SECRET_LEGACY_PROPERTY_NAME)) {
            result.setProperty(SecurityPropertySpecName.PASSWORD.toString(), oldTypedProperties.getStringProperty(HLS_SECRET_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(HEX_PASSWORD_LEGACY_PROPERTY_NAME)) {
            result.setProperty(SecurityPropertySpecName.PASSWORD.toString(), oldTypedProperties.getStringProperty(HEX_PASSWORD_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME)) {
            result.setProperty(SecurityPropertySpecName.ENCRYPTION_KEY.toString(), oldTypedProperties.getStringProperty(DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME)) {
            result.setProperty(SecurityPropertySpecName.AUTHENTICATION_KEY.toString(), oldTypedProperties.getStringProperty(DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME));
        }
        if (oldTypedProperties.hasValueFor(CRYPTOSERVER_LEGACY_PROPERTY_NAME)) {
            result.setProperty(SecurityPropertySpecName.CRYPTOSERVER_PHASE.toString(), oldTypedProperties.getStringProperty(CRYPTOSERVER_LEGACY_PROPERTY_NAME));
        } else {
            result.setProperty(SecurityPropertySpecName.CRYPTOSERVER_PHASE.toString(), DeviceSecurityProperty.CRYPTOSERVER_PHASE.getPropertySpec(this.propertySpecService).getPossibleValues().getDefault());
        }

        return new DeviceProtocolSecurityPropertySet() {
            @Override
            public int getAuthenticationDeviceAccessLevel() {
                return 0;
            }

            @Override
            public int getEncryptionDeviceAccessLevel() {
                return 0;
            }

            @Override
            public TypedProperties getSecurityProperties() {
                return result;
            }
        };
    }

    private void checkForCorrectClientMacAddressPropertySpecType(TypedProperties typedProperties) {
        final Object clientMacAddress = typedProperties.getProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString());
        if (clientMacAddress != null && String.class.isAssignableFrom(clientMacAddress.getClass())) {
            typedProperties.removeProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString());
            try {
                typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), new BigDecimal((String) clientMacAddress));
            } catch (NumberFormatException e) {
                typedProperties.setProperty(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), new BigDecimal("1"));

            }
        }
    }

    private class DefinedByProperty implements AuthenticationDeviceAccessLevel, EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslationKey() {
            return "definedByProperty";
        }

        @Override
        public String getDefaultTranslation() {
            return "Defined by property";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return CryptoDlmsSecuritySupport.this.getSecurityProperties();
        }
    }
}