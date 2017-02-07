/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the {@link MustHaveEitherConnectionSetupOrComTaskExecution} constraint
 * against a {@link DataCollectionKpiImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-06 (15:14)
 */
public class MustHaveEitherConnectionSetupOrComTaskExecutionValidator implements ConstraintValidator<MustHaveEitherConnectionSetupOrComTaskExecution, DataCollectionKpiImpl> {

    @Override
    public void initialize(MustHaveEitherConnectionSetupOrComTaskExecution cantBeOwnGateway) {
        // No need to keep track of the annotation for now
    }

    @Override
    public boolean isValid(DataCollectionKpiImpl value, ConstraintValidatorContext context) {
        if (!(value.calculatesConnectionSetupKpi() || value.calculatesComTaskExecutionKpi())) {
            context.
                    buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).
                    addPropertyNode(DataCollectionKpiImpl.Fields.COMMUNICATION_KPI.fieldName()).
                    addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }

}