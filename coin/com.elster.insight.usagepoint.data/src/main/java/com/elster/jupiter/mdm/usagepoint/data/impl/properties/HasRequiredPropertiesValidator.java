/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl.properties;

import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.properties.ValidationPropertyDefinitionLevel;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HasRequiredPropertiesValidator implements ConstraintValidator<HasRequiredProperties, ChannelValidationRuleOverriddenPropertiesImpl> {

    private final ValidationService validationService;

    @Inject
    public HasRequiredPropertiesValidator(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void initialize(HasRequiredProperties constraintAnnotation) {
        // nothing to do
    }

    @Override
    public boolean isValid(ChannelValidationRuleOverriddenPropertiesImpl overriddenProperties, ConstraintValidatorContext context) {
        Validator validator = getValidator(overriddenProperties);
        Map<String, PropertySpec> propertySpecsOnRule = validator.getPropertySpecs(ValidationPropertyDefinitionLevel.VALIDATION_RULE)
                .stream().collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
        List<PropertySpec> propertySpecsOnUsagePoint = validator.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT);
        Map<String, Object> properties = overriddenProperties.getProperties();

        boolean isValid = true;
        for (PropertySpec propertySpec : propertySpecsOnUsagePoint) {
            if (propertySpec.isRequired() && !propertySpecsOnRule.containsKey(propertySpec.getName()) && !properties.containsKey(propertySpec.getName())) {
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

    private Validator getValidator(ValidationEstimationRuleOverriddenPropertiesImpl overriddenProperties) {
        return this.validationService.getValidator(overriddenProperties.getRuleImpl());
    }
}
