package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

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
            return propertySpecService.basicPropertySpec(this.propertyName(), true, new StringFactory());
        }
    },

    PORT {
        @Override
        public String propertyName() {
            return "port";
        }

        @Override
        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService.basicPropertySpec(this.propertyName(), false, new BigDecimalFactory());
        }
    };

    public abstract String propertyName();

    public abstract PropertySpec propertySpec(PropertySpecService propertySpecService);

}