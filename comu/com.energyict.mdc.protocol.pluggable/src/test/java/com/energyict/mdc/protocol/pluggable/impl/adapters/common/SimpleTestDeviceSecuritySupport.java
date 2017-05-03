/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleTestDeviceSecuritySupport implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    public static final int AUTHENTICATION_DEVICE_ACCESS_LEVEL_ID = 1000;
    public static final int ENCRYPTION_DEVICE_ACCESS_LEVEL_ID = 2000;
    public static final String FIRST = "first";
    public static final String SECOND = "second";
    public static final String THIRD = "third";

    private final PropertySpecService propertySpecService;

    @Inject
    public SimpleTestDeviceSecuritySupport(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
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

    public class SimpleTestAuthenticationDeviceAccessLevel implements AuthenticationDeviceAccessLevel {

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
            return Arrays.asList(
                    propertySpecService
                            .stringSpec()
                            .named(FIRST, FIRST)
                            .describedAs(FIRST)
                            .finish(),
                    propertySpecService
                            .stringSpec()
                            .named(SECOND, SECOND)
                            .describedAs(SECOND)
                            .finish(),
                    propertySpecService
                            .stringSpec()
                            .named(THIRD, THIRD)
                            .describedAs(THIRD)
                            .finish()
            );
        }
    }

    private class SimpleTestEncryptionDeviceAccessLevel implements EncryptionDeviceAccessLevel {

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
                    propertySpecService
                            .stringSpec()
                            .named(FIRST, FIRST)
                            .describedAs(FIRST)
                            .finish(),
                    propertySpecService
                            .stringSpec()
                            .named(SECOND, SECOND)
                            .describedAs(SECOND)
                            .finish(),
                    propertySpecService
                            .stringSpec()
                            .named(THIRD, THIRD)
                            .describedAs(THIRD)
                            .finish()
            );
        }
    }
}