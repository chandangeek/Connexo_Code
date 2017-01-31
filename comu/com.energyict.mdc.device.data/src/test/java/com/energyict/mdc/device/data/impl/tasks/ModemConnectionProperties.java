/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:18)
 */
public enum ModemConnectionProperties {

    PHONE_NUMBER {
        @Override
        public String propertyName() {
            return "phoneNumber";
        }

        @Override
        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService
                    .stringSpec()
                    .named(this.propertyName(), this.propertyName())
                    .describedAs(null)
                    .markRequired()
                    .finish();
        }
    };

    public abstract String propertyName();

    public abstract PropertySpec propertySpec(PropertySpecService propertySpecService);

}