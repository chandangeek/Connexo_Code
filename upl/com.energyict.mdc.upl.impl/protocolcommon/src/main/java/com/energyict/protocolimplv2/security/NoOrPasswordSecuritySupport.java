package com.energyict.protocolimplv2.security;

import com.energyict.cbo.Password;
import com.energyict.comserver.adapters.common.LegacySecurityPropertyConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general security <b>capabilities</b> for device that have either:
 * <ul><li>No password</li>
 * <li>A password</li></ul>
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 14:41
 */
public class NoOrPasswordSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    private final String authenticationTranslationKeyConstant = "NoOrPasswordSecuritySupport.authenticationlevel.";

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.NO_OR_PASSWORD_SECURITY.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.asList(new NoAuthenticationAccessLevel(), new StandardAuthenticationAccessLevel());
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
        }
        return typedProperties;
    }

    /**
     * No authentication level that requires nothing
     */
    protected class NoAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

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
            return Collections.emptyList();
        }
    }

    /**
     * Standard authentication level that requires a password
     */
    protected class StandardAuthenticationAccessLevel implements AuthenticationDeviceAccessLevel {

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
                    DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }
}
