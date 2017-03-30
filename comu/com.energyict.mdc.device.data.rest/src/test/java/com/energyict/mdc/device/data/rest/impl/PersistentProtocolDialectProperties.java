/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-01-06 (16:32)
 */
public class PersistentProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {
    @Size(max = Table.MAX_STRING_LENGTH)
    private String required;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String optional;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.required = (String) propertyValues.getProperty(TestProtocol.REQUIRED_PROPERTY_NAME);
        this.optional = (String) propertyValues.getProperty(TestProtocol.OPTIONAL_PROPERTY_NAME);
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, TestProtocol.REQUIRED_PROPERTY_NAME, this.required);
        this.setPropertyIfNotNull(propertySetValues, TestProtocol.OPTIONAL_PROPERTY_NAME, this.optional);
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}