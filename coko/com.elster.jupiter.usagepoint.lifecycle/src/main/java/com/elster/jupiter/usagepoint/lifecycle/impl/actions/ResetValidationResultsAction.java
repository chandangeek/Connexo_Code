/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResetValidationResultsAction extends TranslatableAction {
    private final ValidationService validationService;

    @Inject
    public ResetValidationResultsAction(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public String getCategory() {
        return MicroCategory.VALIDATION.name();
    }

    @Override
    public boolean isMandatoryForTransition(UsagePointState fromState, UsagePointState toState) {
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    protected void doExecute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) {
        Instant rightAfterTransition = transitionTime.plusMillis(1);
        usagePoint.getEffectiveMetrologyConfigurations().forEach(effectiveMC ->
                effectiveMC.getMetrologyConfiguration().getContracts().stream()
                        .map(effectiveMC::getChannelsContainer)
                        .flatMap(Functions.asStream())
                        .forEach(container -> validationService.moveLastCheckedBefore(container, rightAfterTransition)));
    }
}
