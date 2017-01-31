/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SimpleTestDeviceSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    public static final int AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID = 1000;
    public static final int ENCRYPTION_DEVICE_ACCESS_LEVEL_ID = 2000;

    private final PropertySpecService propertySpecService;

    @Inject
    public SimpleTestDeviceSecuritySupport(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Optional<CustomPropertySet<BaseDevice, ? extends PersistentDomainExtension<BaseDevice>>> getCustomPropertySet() {
        return Optional.of(new SimpleTestDeviceCustomPropertySet(this.propertySpecService));
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return Collections.<AuthenticationDeviceAccessLevel>singletonList(new SimpleTestAuthenticationDeviceAccessLevel());
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return Collections.<EncryptionDeviceAccessLevel>singletonList(new SimpleTestEncryptionDeviceAccessLevel());
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
            return Collections.singletonList(SimpleTestDeviceSecurityProperties.ActualFields.FIRST.propertySpec(propertySpecService));
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
            return Arrays.asList(
                    SimpleTestDeviceSecurityProperties.ActualFields.SECOND.propertySpec(propertySpecService),
                    SimpleTestDeviceSecurityProperties.ActualFields.THIRD.propertySpec(propertySpecService));
        }
    }

}