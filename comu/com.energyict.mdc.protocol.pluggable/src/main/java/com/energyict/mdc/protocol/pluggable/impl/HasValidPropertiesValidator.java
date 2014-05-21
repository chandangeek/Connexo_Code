package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
            context.buildConstraintViolationWithTemplate("{"+MessageSeeds.Constants.PLUGGABLE_CLASS_NEW_INSTANCE_FAILURE+"}").addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }

    private void validatePropertiesAreLinkedToAttributeSpecs(TypedProperties properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.propertyNames()) {
            if (getPropertySpec(propertySpecs, propertyName) == null) {
                context.disableDefaultConstraintViolation();
                context
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.PROTOCOL_DIALECT_PROPERTY_NOT_IN_SPEC_KEY + "}")
                    .addPropertyNode("properties").addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private PropertySpec<?> getPropertySpec(List<PropertySpec> propertySpecs, String name) {
        for (PropertySpec<?> propertySpec : propertySpecs) {
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
        PropertySpec propertySpec=null;
        try {
            propertySpec = getPropertySpec(propertySpecs, propertyName);
            propertySpec.validateValue(propertyValue);
        }
        catch (InvalidValueException e) {
            context.disableDefaultConstraintViolation();
            context
                .buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.PROTOCOL_DIALECT_PROPERTY_INVALID_VALUE_KEY + "}")
                .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation();
            this.valid = false;
        }
    }

}