package com.elster.jupiter.properties.impl;

import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;
import com.elster.jupiter.properties.HasValidProperties;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;

public class HasValidPropertiesValidator implements ConstraintValidator<HasValidProperties, HasDynamicPropertiesWithValues> {

    private static final String PROPERTIES_NODE = "properties";
    
    private HasValidProperties annotation;
    private boolean valid;
    
    @Override
    public void initialize(HasValidProperties constraintAnnotation) {
        this.annotation = constraintAnnotation;
        this.valid = true;// Optimistic approach
    }

    @Override
    public boolean isValid(HasDynamicPropertiesWithValues object, ConstraintValidatorContext context) {
        Map<String, Object> properties = object.getProperties();
        List<PropertySpec> propertySpecs = object.getPropertySpecs();
        this.validatePropertiesAreLinkedToAttributeSpecs(properties, propertySpecs, context);
        this.validateRequiredPropertiesArePresent(properties, propertySpecs, context);
        this.validatePropertyValues(properties, propertySpecs, context);
        return this.valid;
    }

    private void validatePropertiesAreLinkedToAttributeSpecs(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.keySet()) {
            if (getPropertySpec(propertySpecs, propertyName) == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(annotation.propertyNotInSpecMessage())
                       .addPropertyNode(PROPERTIES_NODE)
                       .addPropertyNode(propertyName)
                       .addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validateRequiredPropertiesArePresent(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (PropertySpec propertySpec : propertySpecs) {
            if (propertySpec.isRequired() && !properties.containsKey(propertySpec.getName())) {
                context.buildConstraintViolationWithTemplate(annotation.requiredPropertyMissingMessage())
                       .addPropertyNode(PROPERTIES_NODE)
                       .addPropertyNode(propertySpec.getName())
                       .addConstraintViolation()
                       .disableDefaultConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validatePropertyValues(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            validatePropertyValue(entry.getKey(), entry.getValue(), propertySpecs, context);
        }
    }

    private void validatePropertyValue(String propertyName, Object propertyValue, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        PropertySpec propertySpec = null;
        try {
            propertySpec = getPropertySpec(propertySpecs, propertyName);
            propertySpec.validateValue(propertyValue);
        } catch (InvalidValueException e) {
            context.buildConstraintViolationWithTemplate("{" + e.getMessageId() + "}")
                   .addPropertyNode(PROPERTIES_NODE)
                   .addPropertyNode(propertySpec.getName())
                   .addConstraintViolation()
                   .disableDefaultConstraintViolation();
            this.valid = false;
        }
    }
    
    private PropertySpec getPropertySpec(List<PropertySpec> propertySpecs, String name) {
        return propertySpecs.stream()
                .filter(propertySpec -> propertySpec.getName().equals(name))
                .findFirst().orElse(null);
    }
}
