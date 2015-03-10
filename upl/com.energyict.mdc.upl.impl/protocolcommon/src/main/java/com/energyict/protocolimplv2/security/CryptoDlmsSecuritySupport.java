package com.energyict.protocolimplv2.security;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.*;

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
    private static final String SECURITY_LEVEL_PROPERTY_NAME = "SecurityLevel";
    private static final String DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME = "DataTransportAuthenticationKey";
    private static final String DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME = "DataTransportEncryptionKey";
    private static final String HLS_SECRET_LEGACY_PROPERTY_NAME = "HlsSecret";
    private static final String HEX_PASSWORD_LEGACY_PROPERTY_NAME = "HexPassword";
    private static final String CRYPTOSERVER_LEGACY_PROPERTY_NAME = "CryptoServer";

    @Override
    public List<String> getLegacySecurityProperties() {
        return Arrays.asList(
                SECURITY_LEVEL_PROPERTY_NAME,
                DATA_TRANSPORT_AUTHENTICATION_KEY_LEGACY_PROPERTY_NAME,
                DATA_TRANSPORT_ENCRYPTION_KEY_LEGACY_PROPERTY_NAME,
                HLS_SECRET_LEGACY_PROPERTY_NAME,
                HEX_PASSWORD_LEGACY_PROPERTY_NAME,
                CRYPTOSERVER_LEGACY_PROPERTY_NAME);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(
                DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                DeviceSecurityProperty.AUTHENTICATION_KEY.getPropertySpec(),
                DeviceSecurityProperty.ENCRYPTION_KEY.getPropertySpec(),
                DeviceSecurityProperty.CLIENT_MAC_ADDRESS.getPropertySpec(),
                DeviceSecurityProperty.CRYPTOSERVER_PHASE.getPropertySpec(),
                DeviceSecurityProperty.SECURITY_LEVEL.getPropertySpec()
        );
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.CRYPTO_DLMS_SECURITY.toString();
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
        public List<PropertySpec> getSecurityProperties() {
            return CryptoDlmsSecuritySupport.this.getSecurityProperties();
        }
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
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());    //Adds ClientMacAddress and SecurityLevel, their names have not changed

            // HlsSecret: override the password (as it is provided as a Password object instead of a String
            final Object property = deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), new Password(""));
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
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        checkForCorrectClientMacAddressPropertySpecType(typedProperties);
        final TypedProperties securityRelatedTypedProperties = TypedProperties.empty();
        securityRelatedTypedProperties.setAllProperties(LegacyPropertiesExtractor.getSecurityRelatedProperties(typedProperties, 0, getAuthenticationAccessLevels()));

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
                return securityRelatedTypedProperties;
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
}