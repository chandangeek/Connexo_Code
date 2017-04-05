/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl.properties;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class HasEstimationRuleRequiredPropertiesValidator extends HasRequiredPropertiesValidator<ChannelEstimationRuleOverriddenPropertiesImpl> {

    private final EstimationService estimationService;

    @Inject
    HasEstimationRuleRequiredPropertiesValidator(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Override
    Map<String, PropertySpec> getPropertiesFromLowerLevels(ChannelEstimationRuleOverriddenPropertiesImpl overriddenProperties) {
        return estimationService.getEstimator(overriddenProperties.getRuleImpl())
                .map(estimator -> estimator.getPropertySpecs(EstimationPropertyDefinitionLevel.ESTIMATION_RULE))
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
    }
}
