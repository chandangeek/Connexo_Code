/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimatorNotFoundException;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

public class HasValidPropertiesValidator implements ConstraintValidator<HasValidProperties, EstimationRuleImpl> {

    private boolean valid;

    @Override
    public void initialize(HasValidProperties constraintAnnotation) {
        // No need to keep track of the annotation for now
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(EstimationRuleImpl estimationRule, ConstraintValidatorContext context) {
        try {
            Map<String, Object> properties = estimationRule.getProps();
            List<PropertySpec> propertySpecs = estimationRule.getPropertySpecs(EstimationPropertyDefinitionLevel.ESTIMATION_RULE);
            this.validatePropertiesAreLinkedToAttributeSpecs(properties, propertySpecs, context);
            this.validateRequiredPropertiesArePresent(properties, propertySpecs, context);
            this.validatePropertyValues(properties, propertySpecs, context);
            this.validateAdvanceReadingsSettingsProperty(properties, context);
            return this.valid;
        } catch (EstimatorNotFoundException e) {
            context.buildConstraintViolationWithTemplate(e.getLocalizedMessage()).addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
    }

    private void validateAdvanceReadingsSettingsProperty(Map<String, Object> properties, ConstraintValidatorContext context) {
        String propertyName = "averagewithsamples.advanceReadingsSettings";
        if (properties.containsKey(propertyName)) {
            Object propertyValue = properties.get(propertyName);
            if (propertyValue instanceof ReadingTypeAdvanceReadingsSettings) {
                if (propertyValue.toString().isEmpty()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                            .addPropertyNode(propertyName)
                            .addConstraintViolation();
                    this.valid = false;
                }
            }
        }

    }

    private void validatePropertiesAreLinkedToAttributeSpecs(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (String propertyName : properties.keySet()) {
            if (getPropertySpec(propertySpecs, propertyName) == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.ESTIMATOR_PROPERTY_NOT_IN_SPEC_KEY + "}")
                        .addPropertyNode(propertyName)
                       .addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validateRequiredPropertiesArePresent(Map<String, Object> properties, List<PropertySpec> propertySpecs, ConstraintValidatorContext context) {
        for (PropertySpec propertySpec : propertySpecs) {
            if (propertySpec.isRequired() && !properties.containsKey(propertySpec.getName())) {
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.ESTIMATOR_REQUIRED_PROPERTY_MISSING_KEY + "}")
                       .addPropertyNode(propertySpec.getName())
                       .addConstraintViolation()
                       .disableDefaultConstraintViolation();
                 this.valid = false;
            }
        }
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
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.ESTIMATOR_PROPERTY_INVALID_VALUE_KEY + "}")
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
