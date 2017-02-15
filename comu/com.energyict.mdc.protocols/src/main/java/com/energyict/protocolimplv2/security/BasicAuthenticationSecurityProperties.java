/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * that requires username/password (like in basic authentication)
 * and is known to be used by {@link PasswordWithUserIdentificationSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (10:10)
 */
public class BasicAuthenticationSecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        PASSWORD(DeviceSecurityProperty.PASSWORD) {
            @Override
            public String javaName() {
                return "password";
            }

            @Override
            public String databaseName() {
                return "PASSWORD";
            }
        },
        USER_NAME(DeviceSecurityProperty.DEVICE_ACCESS_IDENTIFIER) {
            @Override
            public String javaName() {
                return "user";
            }

            @Override
            public String databaseName() {
                return "USER_NME";
            }
        };

        private final DeviceSecurityProperty deviceSecurityProperty;

        ActualFields(DeviceSecurityProperty deviceSecurityProperty) {
            this.deviceSecurityProperty = deviceSecurityProperty;
        }

        public abstract String javaName();

        public abstract String databaseName();

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

        public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return this.deviceSecurityProperty.getPropertySpec(propertySpecService, thesaurus);
        }

    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String user;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) getTypedPropertyValue(propertyValues, DeviceSecurityProperty.PASSWORD.javaName());
        this.user = (String) getTypedPropertyValue(propertyValues, SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.getKey());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            setTypedPropertyValueTo(propertySetValues, DeviceSecurityProperty.PASSWORD.javaName(), this.password);
        }
        this.setPropertyIfNotNull(propertySetValues, SecurityPropertySpecName.DEVICE_ACCESS_IDENTIFIER.getKey(), this.user);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}