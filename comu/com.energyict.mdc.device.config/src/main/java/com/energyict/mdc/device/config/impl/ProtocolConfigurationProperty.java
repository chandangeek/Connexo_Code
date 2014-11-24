package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;

/**
 * Models a single key-value pair that holds onto the
 * general protocol properties on the configuration level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (09:32)
 */
class ProtocolConfigurationProperty {

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @Size(min = 1, max = 255, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @Size(min = 1, max = 4000, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String value;

    static ProtocolConfigurationProperty forNameAndValue(String name, String value, DeviceConfiguration deviceConfiguration) {
        ProtocolConfigurationProperty property = new ProtocolConfigurationProperty();
        property.name = name;
        property.value = value;
        property.deviceConfiguration.set(deviceConfiguration);
        return property;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public ProtocolConfigurationProperty setValue(String value) {
        this.value = value;
        return this;
    }

}