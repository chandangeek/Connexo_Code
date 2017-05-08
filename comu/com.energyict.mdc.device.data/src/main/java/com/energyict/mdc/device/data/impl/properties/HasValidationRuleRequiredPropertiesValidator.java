/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class HasValidationRuleRequiredPropertiesValidator extends HasRequiredPropertiesValidator<ChannelValidationRuleOverriddenPropertiesImpl> {

    private final ValidationService validationService;

    @Inject
    HasValidationRuleRequiredPropertiesValidator(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    Map<String, PropertySpec> getPropertiesFromLowerLevels(ChannelValidationRuleOverriddenPropertiesImpl overriddenProperties) {
        return validationService.getValidator(overriddenProperties.getRuleImpl())
                .getPropertySpecs(ValidationPropertyDefinitionLevel.VALIDATION_RULE)
                .stream()
                .collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
    }
}
