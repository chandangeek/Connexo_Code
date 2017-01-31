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
public enum IpConnectionProperties {

    IP_ADDRESS {
        @Override
        public String propertyName() {
            return "ipAddress";
        }

        @Override
        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService
                    .stringSpec()
                    .named(this.propertyName(), this.propertyName())
                    .describedAs("Description for " + this.propertyName())
                    .markRequired()
                    .finish();
        }
    },

    PORT {
        @Override
        public String propertyName() {
            return "port";
        }

        @Override
        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService
                    .bigDecimalSpec()
                    .named(this.propertyName(), this.propertyName())
                    .describedAs("Description for " + this.propertyName())
                    .finish();
        }
    };

    public abstract String propertyName();

    public abstract PropertySpec propertySpec(PropertySpecService propertySpecService);

}