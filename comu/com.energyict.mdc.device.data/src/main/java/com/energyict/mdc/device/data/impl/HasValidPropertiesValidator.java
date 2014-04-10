package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the {@link HasValidProperties} constraint against a {@link ProtocolDialectPropertiesImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-07 (13:55)
 */
public class HasValidPropertiesValidator implements ConstraintValidator<HasValidProperties, ProtocolDialectPropertiesImpl> {

    private boolean valid;

    @Override
    public void initialize(HasValidProperties constraintAnnotation) {
        // No need to keep track of the annotation for now
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(ProtocolDialectPropertiesImpl protocolDialectProperties, ConstraintValidatorContext context) {
        DeviceProtocol deviceProtocol = protocolDialectProperties.getDeviceProtocol();
        DeviceProtocolDialect deviceProtocolDialect = this.getDeviceProtocolDialectFor(deviceProtocol, protocolDialectProperties.getDeviceProtocolDialectName());
        TypedProperties properties = protocolDialectProperties.getTypedProperties();
        this.validatePropertiesAreLinkedToAttributeSpecs(properties, deviceProtocolDialect, context);
        this.validatePropertyValues(properties, deviceProtocolDialect, context);
        this.validateAllRequiredPropertiesHaveValues(properties, deviceProtocolDialect, context);
        return this.valid;
    }

    private DeviceProtocolDialect getDeviceProtocolDialectFor(DeviceProtocol deviceProtocol, String name) {
        for (DeviceProtocolDialect deviceProtocolDialect : deviceProtocol.getDeviceProtocolDialects()) {
            if (deviceProtocolDialect.getDeviceProtocolDialectName().equals(name)) {
                return deviceProtocolDialect;
            }
        }
        return null;
    }

    private void validatePropertiesAreLinkedToAttributeSpecs(TypedProperties properties, DeviceProtocolDialect deviceProtocolDialect, ConstraintValidatorContext context) {
        for (String propertyName : properties.propertyNames()) {
            if (deviceProtocolDialect.getPropertySpec(propertyName) == null) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.DEVICE_PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY + "}")
                    .addPropertyNode("properties").addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validatePropertyValues(TypedProperties properties, DeviceProtocolDialect deviceProtocolDialect, ConstraintValidatorContext context) {
        for (String propertyName : properties.propertyNames()) {
            this.validatePropertyValue(propertyName, properties.getProperty(propertyName), deviceProtocolDialect, context);
        }
    }

    @SuppressWarnings("unchecked")
    private void validatePropertyValue(String propertyName, Object propertyValue, DeviceProtocolDialect deviceProtocolDialect, ConstraintValidatorContext context) {
        try {
            PropertySpec propertySpec = deviceProtocolDialect.getPropertySpec(propertyName);
            propertySpec.validateValue(propertyValue);
        }
        catch (InvalidValueException e) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.DEVICE_PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY + "}")
                .addPropertyNode("properties").addConstraintViolation();
            this.valid = false;
        }
    }

    private void validateAllRequiredPropertiesHaveValues(TypedProperties properties, DeviceProtocolDialect deviceProtocolDialect, ConstraintValidatorContext context) {
        Set<String> propertyNames = new HashSet<>(properties.propertyNames());
        for (PropertySpec propertySpec : this.getRequiredProperties(deviceProtocolDialect)) {
            if (!propertyNames.contains(propertySpec.getName())) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.DEVICE_PROTOCOL_DIALECT_REQUIRED_PROPERTY_MISSING_KEY + "}")
                    .addPropertyNode("properties").addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private List<PropertySpec> getRequiredProperties (DeviceProtocolDialect deviceProtocolDialect) {
        List<PropertySpec> requiredPropertySpecs = new ArrayList<>();
        for (PropertySpec propertySpec : deviceProtocolDialect.getPropertySpecs()) {
            if (propertySpec.isRequired()) {
                requiredPropertySpecs.add(propertySpec);
            }
        }
        return requiredPropertySpecs;
    }

}