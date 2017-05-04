/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple implementation of a {@link DeviceSecuritySupport} component.
 * Will only be used for testing
 * <p>
 * Date: 15/01/13
 * Time: 10:54
 */
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
    public List<PropertySpec> getSecurityProperties() {
        return getSecurityPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::new).collect(Collectors.toList());
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
        public String getTranslationKey() {
            return "Simple Test Authentication Device Access Level";
        }

        @Override
        public String getDefaultTranslation() {
            return "Simple Test Authentication Device Access Level";
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    new ConnexoToUPLPropertSpecAdapter(
                            propertySpecService
                            .stringSpec()
                            .named(FIRST, FIRST)
                            .describedAs(FIRST)
                            .finish()),
                    new ConnexoToUPLPropertSpecAdapter(
                            propertySpecService
                            .stringSpec()
                            .named(SECOND, SECOND)
                            .describedAs(SECOND)
                            .finish()),
                    new ConnexoToUPLPropertSpecAdapter(
                            propertySpecService
                            .stringSpec()
                            .named(THIRD, THIRD)
                            .describedAs(THIRD)
                            .finish())
            );
        }
    }

    private class SimpleTestEncryptionDeviceAccessLevel implements EncryptionDeviceAccessLevel {

        @Override
        public int getId() {
            return ENCRYPTION_DEVICE_ACCESS_LEVEL_ID;
        }

        @Override
        public String getTranslationKey() {
            return "Simple Test Encryption Device Access Level";
        }

        @Override
        public String getDefaultTranslation() {
            return "Simple Test Encryption Device Access Level";
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Arrays.asList(
                    new ConnexoToUPLPropertSpecAdapter(
                            propertySpecService
                            .stringSpec()
                            .named(FIRST, FIRST)
                            .describedAs(FIRST)
                            .finish()                     ),
                    new ConnexoToUPLPropertSpecAdapter(
                            propertySpecService
                            .stringSpec()
                            .named(SECOND, SECOND)
                            .describedAs(SECOND)
                            .finish()                    ),
                    new ConnexoToUPLPropertSpecAdapter(
                            propertySpecService
                            .stringSpec()
                            .named(THIRD, THIRD)
                            .describedAs(THIRD)
                            .finish()                     )
            );
        }
    }
}