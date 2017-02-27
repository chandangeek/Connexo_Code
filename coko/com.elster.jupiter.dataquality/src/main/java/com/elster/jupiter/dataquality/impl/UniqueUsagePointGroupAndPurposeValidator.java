/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

import static com.elster.jupiter.util.streams.Predicates.not;

public class UniqueUsagePointGroupAndPurposeValidator implements ConstraintValidator<UniqueUsagePointGroupAndPurpose, UsagePointDataQualityKpiImpl> {

    private final DataQualityKpiService dataQualityKpiService;

    private String message;

    @Inject
    public UniqueUsagePointGroupAndPurposeValidator(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @Override
    public void initialize(UniqueUsagePointGroupAndPurpose constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(UsagePointDataQualityKpiImpl dataQualityKpi, ConstraintValidatorContext context) {
        if (dataQualityKpi.getMetrologyPurpose() == null || dataQualityKpi.getUsagePointGroup() == null) {
            return true;// @IsPresent constraint violation should work
        }
        List<UsagePointDataQualityKpi> existingKpis = dataQualityKpiService.usagePointDataQualityKpiFinder()
                .forGroup(dataQualityKpi.getUsagePointGroup())
                .forPurpose(dataQualityKpi.getMetrologyPurpose())
                .find();
        if (existingKpis.stream().anyMatch(not(dataQualityKpi::equals))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(UsagePointDataQualityKpiImpl.Fields.USAGEPOINT_GROUP.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
