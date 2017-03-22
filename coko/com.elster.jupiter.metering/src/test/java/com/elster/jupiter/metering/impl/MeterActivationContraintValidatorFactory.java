/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

public class MeterActivationContraintValidatorFactory implements ConstraintValidatorFactory {
    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    public MeterActivationContraintValidatorFactory(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> aClass) {
        if (aClass.equals(MeterRolePartOfMetrologyConfigurationIfAnyValidator.class)) {
            return (T) new MeterRolePartOfMetrologyConfigurationIfAnyValidator(thesaurus);
        } else if (aClass.equals(MeterInCorrectStageValidator.class)) {
            return (T) new MeterInCorrectStageValidator();
        }
        return dataModel.getInstance(aClass);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> constraintValidator) {

    }
}
