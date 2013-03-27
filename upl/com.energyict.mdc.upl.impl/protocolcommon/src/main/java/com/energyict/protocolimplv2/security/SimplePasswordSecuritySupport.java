package com.energyict.protocolimplv2.security;

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
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password to do authentication/encryption
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/01/13
 * Time: 14:47
 */
public class SimplePasswordSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
    }

    @Override
    public String getSecurityRelationTypeName() {
        return SecurityRelationTypeName.SIMPLE_PASSWORD.toString();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.<AuthenticationDeviceAccessLevel>asList(new SimpleAuthentication());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        if (DeviceSecurityProperty.PASSWORD.getPropertySpec().getName().equals(name)) {
            return DeviceSecurityProperty.PASSWORD.getPropertySpec();
        } else {
            return null;
        }
    }

    @Override
    public TypedProperties convertToTypedProperties(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        if (deviceProtocolSecurityPropertySet != null) {
            typedProperties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
            // override the password (as it is provided as a Password object instead of a String
            typedProperties.setProperty(SecurityPropertySpecName.PASSWORD.toString(),
                    deviceProtocolSecurityPropertySet.getSecurityProperties().getProperty(SecurityPropertySpecName.PASSWORD.toString(), ""));
        }
        return typedProperties;
    }

    /**
     * A simple authentication level that requires a single password
     */
    protected class SimpleAuthentication implements AuthenticationDeviceAccessLevel {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getTranslationKey() {
            return "SimplePasswordSecuritySupport.authenticationlevel." + getId();
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        }
    }
}
