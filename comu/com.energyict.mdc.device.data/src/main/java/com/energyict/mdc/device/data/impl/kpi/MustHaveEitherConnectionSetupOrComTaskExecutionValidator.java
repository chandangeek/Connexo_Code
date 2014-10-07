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
        return value.calculatesConnectionSetupKpi() || value.calculatesComTaskExecutionKpi();
    }

}