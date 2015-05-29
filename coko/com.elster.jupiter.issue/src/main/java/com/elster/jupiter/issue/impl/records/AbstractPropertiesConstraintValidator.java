package com.elster.jupiter.issue.impl.records;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;

public abstract class AbstractPropertiesConstraintValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    private static final String PROPERTIES_NODE = "properties";
    
    private Thesaurus thesaurus;
    private boolean valid;
    
    public AbstractPropertiesConstraintValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(A constraintAnnotation) {
        // No need to keep track of the annotation for now
        this.valid = true; // Optimistic approach
    }

    @Override
    public boolean isValid(T t, ConstraintValidatorContext context) {
        Map<String, Object> properties = getProps(t);
        List<PropertySpec> propertySpecs = getPropertySpecs(t);
        this.validatePropertiesAreLinkedToAttributeSpecs(properties, propertySpecs, context);
        this.validateRequiredPropertiesArePresent(properties, propertySpecs, context);
        this.validatePropertyValues(properties, propertySpecs, context);
        return this.valid;
    }
    
    protected abstract Map<String, Object> getProps(T t);
    
    protected abstract List<PropertySpec> getPropertySpecs(T t);

    private void validatePropertiesAreLinkedToAttributeSpecs(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.keySet()) {
            if (getPropertySpec(propertySpecs, propertyName) == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(MessageSeeds.PROPERTY_NOT_IN_PROPERTYSPECS.getTranslated(thesaurus))
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
                context.buildConstraintViolationWithTemplate(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getTranslated(thesaurus))
                       .addPropertyNode(PROPERTIES_NODE)
                       .addPropertyNode(propertySpec.getName()).addConstraintViolation().disableDefaultConstraintViolation();
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
            context.buildConstraintViolationWithTemplate(MessageSeeds.PROPERTY_INVALID_VALUE.getTranslated(thesaurus))
                   .addPropertyNode(PROPERTIES_NODE)
                   .addPropertyNode(propertySpec.getName()).addConstraintViolation().disableDefaultConstraintViolation();
            this.valid = false;
        }
    }

    private PropertySpec getPropertySpec(List<PropertySpec> propertySpecs, String name) {
        return propertySpecs.stream().filter(propertySpec -> propertySpec.getName().equals(name)).findFirst().orElse(null);
    }
   
}
