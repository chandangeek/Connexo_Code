/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl.properties;

import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.properties.PropertySpec;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

abstract class HasRequiredPropertiesValidator<T extends ValidationEstimationRuleOverriddenPropertiesImpl> implements ConstraintValidator<HasRequiredProperties, T> {

    @Override
    public void initialize(HasRequiredProperties constraintAnnotation) {
        // nothing to do
    }

    abstract Map<String, PropertySpec> getPropertiesFromLowerLevels(T overriddenProperties);

    @Override
    public boolean isValid(T overriddenProperties, ConstraintValidatorContext context) {
        List<PropertySpec> canBeOverriddenProperties = overriddenProperties.getPropertySpecs();
        Map<String, Object> properties = overriddenProperties.getProperties();
        Map<String, PropertySpec> propertiesFromLowerLevels = getPropertiesFromLowerLevels(overriddenProperties);

        boolean isValid = true;
        for (PropertySpec propertySpec : canBeOverriddenProperties) {
            if (propertySpec.isRequired() && !propertiesFromLowerLevels.containsKey(propertySpec.getName()) && !properties.containsKey(propertySpec.getName())) {
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.FIELD_IS_REQUIRED + "}")
                        .addPropertyNode("properties")
                        .addPropertyNode(propertySpec.getName())
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                isValid = false;
            }
        }
        return isValid;
    }
}
