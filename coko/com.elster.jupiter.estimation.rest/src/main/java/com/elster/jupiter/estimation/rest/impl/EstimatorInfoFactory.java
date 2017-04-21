/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import java.util.List;

class EstimatorInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    EstimatorInfoFactory(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    private EstimatorInfo asInfo(Estimator estimator, List<PropertySpec> propertySpecs) {
        EstimatorInfo info = new EstimatorInfo();
        info.implementation = estimator.getClass().getName();
        info.displayName = estimator.getDisplayName();
        info.properties = propertyValueInfoService.getPropertyInfos(propertySpecs);
        return info;
    }

    EstimatorInfo asInfo(Estimator estimator, EstimationPropertyDefinitionLevel propertyDefinitionLevel) {
        return asInfo(estimator, estimator.getPropertySpecs(propertyDefinitionLevel));
    }

    EstimatorInfo asInfo(Estimator estimator) {
        return asInfo(estimator, estimator.getPropertySpecs());
    }
}
