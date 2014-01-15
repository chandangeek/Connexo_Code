package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocols.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;

import java.util.Arrays;
import java.util.List;

/**
 * Simple implementation of a {@link DeviceSecuritySupport} component.
 * Will only be used for testing
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/01/13
 * Time: 10:54
 */
public class SimpleTestDeviceSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    public static final String DUMMY_RELATION_TYPE_NAME = "DummySRTN";
    public static final String FIRST_PROPERTY_NAME = "FirstPropertyName";
    public static final String SECOND_PROPERTY_NAME = "SecondPropertyName";
    public static final String THIRD_PROPERTY_NAME = "ThirdPropertyName";
    public static final PropertySpec firstPropSpec = OptionalPropertySpecFactory.newInstance().stringPropertySpec(FIRST_PROPERTY_NAME);
    public static final PropertySpec secondPropSpec = OptionalPropertySpecFactory.newInstance().stringPropertySpec(SECOND_PROPERTY_NAME);
    public static final PropertySpec thirdPropSpec = OptionalPropertySpecFactory.newInstance().stringPropertySpec(THIRD_PROPERTY_NAME);
    public static final int AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID = 1000;
    public static final int ENCRYPTION_DEVICE_ACCESS_LEVEL_ID = 2000;

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Arrays.asList(firstPropSpec, secondPropSpec, thirdPropSpec);
    }

    @Override
    public String getSecurityRelationTypeName() {
        return DUMMY_RELATION_TYPE_NAME;
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Arrays.<AuthenticationDeviceAccessLevel>asList(new SimpleTestAuthenticationDeviceAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Arrays.<EncryptionDeviceAccessLevel>asList(new SimpleTestEncryptionDeviceAccessLevel());
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DeviceProtocolSecurityPropertySet convertFromTypedProperties(TypedProperties typedProperties) {
        return null;
    }

    public class SimpleTestAuthenticationDeviceAccessLevel implements AuthenticationDeviceAccessLevel{

        @Override
        public int getId() {
            return AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslationKey() {
            return "SimpleTestAuthenticationDeviceAccessLevel.translationKey";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(firstPropSpec);
        }
    }

    private class SimpleTestEncryptionDeviceAccessLevel implements EncryptionDeviceAccessLevel{

        @Override
        public int getId() {
            return ENCRYPTION_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslationKey() {
            return "SimpleTestEncryptionDeviceAccessLevel.translationkey";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(secondPropSpec, thirdPropSpec);
        }
    }
}
