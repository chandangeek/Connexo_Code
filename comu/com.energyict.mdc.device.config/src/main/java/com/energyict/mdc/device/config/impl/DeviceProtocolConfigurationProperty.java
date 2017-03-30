/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;
import java.text.MessageFormat;
import java.time.Instant;

/**
 * Models a single key-value pair that holds onto the
 * general protocol properties on the configuration level.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-24 (09:32)
 */
@IsValidProperty
class DeviceProtocolConfigurationProperty {

    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();

    @Size(min = 1, max = 255, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @Size(min = 1, max = 4000, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String value;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
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

    boolean validate(ConstraintValidatorContext context) {
        PropertySpec propertySpec = getPropertySpec();
        try {
            Object actualValue = value;
            try {
                actualValue = propertySpec.getValueFactory().fromStringValue(value);
            } catch (Exception e) {
                // if conversion fails, validation will fail
            }
            return propertySpec.validateValue(actualValue);
        } catch (InvalidValueException e) {
            context.buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                    .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
    }

    private PropertySpec getPropertySpec() {
        return deviceConfiguration.get()
                .getDeviceProtocolProperties()
                .getPropertySpecs()
                .stream()
                .filter(spec -> spec.getName().equals(name))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}