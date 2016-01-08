package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.dynamic.PropertySpecService;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

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
            return propertySpecService.basicPropertySpec(this.propertyName(), true, new StringFactory());
        }
    };

    public abstract String propertyName();

    public abstract PropertySpec propertySpec(PropertySpecService propertySpecService);

}