/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.util.List;

public class HasValidPropertiesValidator implements ConstraintValidator<HasValidProperties, DeviceProtocolPluggableClassImpl> {

    private boolean valid;

    @Override
    public void initialize(HasValidProperties constraintAnnotation) {
        // No need to keep track of the annotation for now
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(DeviceProtocolPluggableClassImpl deviceProtocolPluggableClass, ConstraintValidatorContext context) {
        try {
            TypedProperties properties = deviceProtocolPluggableClass.getProperties();
            List<PropertySpec> propertySpecs = deviceProtocolPluggableClass.getPropertySpecs();
            this.validatePropertiesAreLinkedToAttributeSpecs(properties, propertySpecs, context);
            this.validatePropertyValues(properties, propertySpecs, context);
            return this.valid;
        } catch (ProtocolCreationException e) {
            context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE+"}").addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }

    private void validatePropertiesAreLinkedToAttributeSpecs(TypedProperties properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.propertyNames()) {
            if (getPropertySpec(propertySpecs, propertyName) == null) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY + "}")
                    .addPropertyNode("properties").addPropertyNode(propertyName).addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private PropertySpec getPropertySpec(List<PropertySpec> propertySpecs, String name) {
        for (PropertySpec propertySpec : propertySpecs) {
            if (name.equals(propertySpec.getName())) {
                return propertySpec;
            }
        }
        return null;
    }


    private void validatePropertyValues(TypedProperties properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.propertyNames()) {
            this.validatePropertyValue(propertyName, properties.getProperty(propertyName), propertySpecs, context);
        }
    }

    @SuppressWarnings("unchecked")
    private void validatePropertyValue(String propertyName, Object propertyValue, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        PropertySpec propertySpec = null;
        try {
            propertySpec = getPropertySpec(propertySpecs, propertyName);
            propertySpec.validateValue(propertyValue);
        }
        catch (InvalidValueException e) {
            context
                .buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation()
                .disableDefaultConstraintViolation();
            this.valid = false;
        }
    }

}