/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;

import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * that requires username/password (like in basic authentication).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (11:13)
 */
public class BasicAuthenticationSecurityProperties extends CommonBaseDeviceSecurityProperties {

    public enum ActualFields {
        PASSWORD("password"){},
        USER_NAME("usrName");

        private final String name;

        ActualFields(String name) {
            this.name = name;
        }

        public String javaName() {
            return this.name;
        };

        public String databaseName() {
            return this.javaName().toUpperCase();
        };

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService
                    .specForValuesOf(new StringFactory())
                    .named(javaName(), javaName()).describedAs("Description for " + javaName())
                    .finish();
        }

    }

    @Size(max = Table.MAX_STRING_LENGTH)
    private String password;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String usrName; // Avoid clash with userName field that holds audit information

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.password = (String) propertyValues.getProperty(ActualFields.PASSWORD.javaName());
        this.usrName = (String) propertyValues.getProperty(ActualFields.USER_NAME.javaName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        if (!is(this.password).empty()) {
            propertySetValues.setProperty(ActualFields.PASSWORD.javaName(), this.password);
        }
        this.setPropertyIfNotNull(propertySetValues, ActualFields.USER_NAME.javaName(), this.usrName);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}