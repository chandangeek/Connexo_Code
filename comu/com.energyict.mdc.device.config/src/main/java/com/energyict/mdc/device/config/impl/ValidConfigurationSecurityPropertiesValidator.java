/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Validates the {@link ValidConfigurationSecurityProperties} constraint against a {@link SecurityPropertySetImpl}.
 *
 * @author stijn
 * @since 26.04.17 - 10:20
 */
public class ValidConfigurationSecurityPropertiesValidator implements ConstraintValidator<ValidConfigurationSecurityProperties, SecurityPropertySetImpl> {

    private boolean valid;

    @Override
    public void initialize(ValidConfigurationSecurityProperties constraintAnnotation) {
        this.valid = true;  // Optimistic approach
    }

    @Override
    public boolean isValid(SecurityPropertySetImpl securityPropertySet, ConstraintValidatorContext context) {
        Set<PropertySpec> propertySpecs = securityPropertySet.getPropertySpecs();
        List<ConfigurationSecurityProperty> configurationSecurityProperties = securityPropertySet.getConfigurationSecurityProperties();

        this.validatePropertiesAreLinkedToPropertySpecs(propertySpecs, configurationSecurityProperties, context);
        this.validateAllRequiredPropertiesHaveValues(propertySpecs, configurationSecurityProperties, context);
        this.validatePropertyValues(propertySpecs, configurationSecurityProperties, context);
        return this.valid;
    }

    private void validatePropertiesAreLinkedToPropertySpecs(Set<PropertySpec> propertySpecs, List<ConfigurationSecurityProperty> configurationSecurityProperties, ConstraintValidatorContext context) {
        for (ConfigurationSecurityProperty configurationSecurityProperty : configurationSecurityProperties) {
            if (!propertySpecs.stream().anyMatch(spec -> spec.getName().equals(configurationSecurityProperty.getName()))) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.SECURITY_PROPERTY_SET_PROPERTY_NOT_IN_SPEC + "}")
                        .addPropertyNode("properties").addPropertyNode(configurationSecurityProperty.getName()).addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private void validateAllRequiredPropertiesHaveValues(Set<PropertySpec> propertySpecs, List<ConfigurationSecurityProperty> configurationSecurityProperties, ConstraintValidatorContext context) {
        for (PropertySpec propertySpec : this.getRequiredPropertySpecs(propertySpecs)) {
            String propertySpecName = propertySpec.getName();
            if (!hasValue(configurationSecurityProperties, propertySpecName)) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.SECURITY_PROPERTY_SET_REQUIRED_PROPERTY_MISSING + "}")
                        .addPropertyNode("properties").addPropertyNode(propertySpec.getName()).addConstraintViolation();
                this.valid = false;
            }
        }
    }

    private boolean hasValue(List<ConfigurationSecurityProperty> configurationSecurityProperties, String propertyName) {
        Optional<ConfigurationSecurityProperty> securityPropertyOptional = configurationSecurityProperties
                .stream()
                .filter(property -> property.getName().equals(propertyName))
                .findFirst();
        return securityPropertyOptional.isPresent() && securityPropertyOptional.get().getSecurityAccessorType() != null;
    }

    private List<PropertySpec> getRequiredPropertySpecs(Set<PropertySpec> propertySpecs) {
        List<PropertySpec> requiredProperties = new ArrayList<>(propertySpecs.size());   // Worst case: all specs are required
        for (PropertySpec propertySpec : propertySpecs) {
            if (propertySpec.isRequired()) {
                requiredProperties.add(propertySpec);
            }
        }
        return requiredProperties;
    }

    private void validatePropertyValues(Set<PropertySpec> propertySpecs, List<ConfigurationSecurityProperty> configurationSecurityProperties, ConstraintValidatorContext context) {
        for (ConfigurationSecurityProperty configurationSecurityProperty : configurationSecurityProperties) {
            Optional<PropertySpec> propertySpepropertySpecOptional = propertySpecs
                    .stream()
                    .filter(spec -> spec.getName().equals(configurationSecurityProperty.getName()))
                    .findFirst();
            this.validatePropertyValue(propertySpepropertySpecOptional, configurationSecurityProperty, context);
        }
    }

    private void validatePropertyValue(Optional<PropertySpec> propertySpec, ConfigurationSecurityProperty configurationSecurityProperty, ConstraintValidatorContext context) {
        if (propertySpec.isPresent()) {
            try {
                propertySpec.get().validateValue(configurationSecurityProperty.getSecurityAccessorType());
            } catch (InvalidValueException e) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                        .addPropertyNode("properties").addPropertyNode(propertySpec.get().getName()).addConstraintViolation();
                this.valid = false;
            }
        } // Else if not present, then don't validate anything (but note that previous validations should have already flagged this as not valid)
    }
}