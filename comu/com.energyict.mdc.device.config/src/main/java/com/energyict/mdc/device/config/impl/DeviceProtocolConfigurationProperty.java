package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Models a single key-value pair that holds onto the
 * general protocol properties on the configuration level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (09:32)
 */
class DeviceProtocolConfigurationProperty {

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @Size(min = 1, max = 255, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @Size(min = 1, max = 4000, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String value;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    static DeviceProtocolConfigurationProperty forNameAndValue(String name, String value, DeviceConfiguration deviceConfiguration) {
        DeviceProtocolConfigurationProperty property = new DeviceProtocolConfigurationProperty();
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

    public DeviceProtocolConfigurationProperty setValue(String value) {
        this.value = value;
        return this;
    }

}