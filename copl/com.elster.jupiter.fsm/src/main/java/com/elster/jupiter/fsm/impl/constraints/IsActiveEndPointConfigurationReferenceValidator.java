package com.elster.jupiter.fsm.impl.constraints;

import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsActiveEndPointConfigurationReferenceValidator implements ConstraintValidator<IsActive, Reference<EndPointConfiguration>> {
    private final Thesaurus thesaurus;

    @Inject
    public IsActiveEndPointConfigurationReferenceValidator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void initialize(IsActive constraintAnnotation) {
    }

    @Override
    public boolean isValid(Reference<EndPointConfiguration> value, ConstraintValidatorContext context) {
        if (!value.isPresent() || value.get().isActive()) {
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(thesaurus.getSimpleFormat(MessageSeeds.CAN_NOT_BE_INACTIVE).format(value.get().getName()))
                    .addConstraintViolation();
            return false;

        }
    }
}

