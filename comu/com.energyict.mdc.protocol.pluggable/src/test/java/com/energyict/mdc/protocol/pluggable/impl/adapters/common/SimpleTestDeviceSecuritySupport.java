package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import javax.inject.Inject;
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
    public static final int AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID = 1000;
    public static final int ENCRYPTION_DEVICE_ACCESS_LEVEL_ID = 2000;

    public PropertySpec firstPropSpec;
    public PropertySpec secondPropSpec;
    public PropertySpec thirdPropSpec;

    @Inject
    public SimpleTestDeviceSecuritySupport(PropertySpecService propertySpecService) {
        super();
        this.firstPropSpec = propertySpecService.basicPropertySpec(FIRST_PROPERTY_NAME, false, StringFactory.class);
        this.secondPropSpec = propertySpecService.basicPropertySpec(SECOND_PROPERTY_NAME, false, StringFactory.class);
        this.thirdPropSpec = propertySpecService.basicPropertySpec(THIRD_PROPERTY_NAME, false, StringFactory.class);
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
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
        for (PropertySpec securityProperty : getSecurityPropertySpecs()) {
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
        public String getTranslation() {
            return "Simple Test Authentication Device Access Level";
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
        public String getTranslation() {
            return "Simple Test Encryption Device Access Level";
        }

        @Override
        public List<PropertySpec> getSecurityProperties() {
            return Arrays.asList(secondPropSpec, thirdPropSpec);
        }
    }
}
