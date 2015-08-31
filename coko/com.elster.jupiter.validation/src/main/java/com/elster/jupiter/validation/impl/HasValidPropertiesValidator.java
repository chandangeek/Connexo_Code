package com.elster.jupiter.validation.impl;

import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.ValidatorNotFoundException;

public class HasValidPropertiesValidator implements ConstraintValidator<HasValidProperties, ValidationRuleImpl> {

    private boolean valid;

    @Override
    public void initialize(HasValidProperties constraintAnnotation) {
        // No need to keep track of the annotation for now
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(ValidationRuleImpl validationRule, ConstraintValidatorContext context) {
        try {
            Map<String, Object> properties = validationRule.getProps();
            List<PropertySpec> propertySpecs = validationRule.getPropertySpecs();
            this.validatePropertiesAreLinkedToAttributeSpecs(properties, propertySpecs, context);
            this.validateRequiredPropertiesArePresent(properties, propertySpecs, context);
            this.validatePropertyValues(properties, propertySpecs, context);
            return this.valid;
        } catch (ValidatorNotFoundException e) {
            context.buildConstraintViolationWithTemplate(e.getLocalizedMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }

    private void validatePropertiesAreLinkedToAttributeSpecs(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.keySet()) {
            if (getPropertySpec(propertySpecs, propertyName) == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.VALIDATOR_PROPERTY_NOT_IN_SPEC_KEY + "}")
                       .addPropertyNode("properties")
                       .addPropertyNode(propertyName)
                       .addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validateRequiredPropertiesArePresent(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        propertySpecs.stream().filter(propertySpec -> propertySpec.isRequired() &&
                (!properties.containsKey(propertySpec.getName()) || isEmptyValue(propertySpec, properties.get(propertySpec.getName()))))
                .forEach(propertySpec -> {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.VALIDATOR_REQUIRED_PROPERTY_MISSING_KEY + "}")
                            .addPropertyNode("properties")
                            .addPropertyNode(propertySpec.getName())
                            .addConstraintViolation()
                            .disableDefaultConstraintViolation();
                    this.valid = false;
                });
    }

    private boolean isEmptyValue(PropertySpec propertySpec, Object property) {
        return propertySpec.getValueFactory().toStringValue(property).isEmpty();
    }

    private void validatePropertyValues(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (Map.Entry<String, Object> entry: properties.entrySet()) {
            validatePropertyValue(entry.getKey(), entry.getValue(), propertySpecs, context);
        }
    }

    private void validatePropertyValue(String propertyName, Object propertyValue, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        PropertySpec propertySpec = null;
        try {
            propertySpec = getPropertySpec(propertySpecs, propertyName);
            propertySpec.validateValue(propertyValue);
        } catch (InvalidValueException e) {
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.VALIDATOR_PROPERTY_INVALID_VALUE_KEY + "}")
                   .addPropertyNode("properties")
                   .addPropertyNode(propertySpec.getName())
                   .addConstraintViolation()
                   .disableDefaultConstraintViolation();
            this.valid = false;
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
}