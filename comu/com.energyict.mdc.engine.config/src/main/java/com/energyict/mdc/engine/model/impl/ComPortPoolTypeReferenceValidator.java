package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.engine.model.InboundComPortPool;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ComPortPoolTypeReferenceValidator implements ConstraintValidator<ComPortPoolTypeMatchesComPortType, Reference<InboundComPortPool>> {

    @Override
    public void initialize(ComPortPoolTypeMatchesComPortType constraintAnnotation) {
    }

    @Override
    public boolean isValid(Reference<InboundComPortPool> comPortPoolReference, ConstraintValidatorContext context) {
        InboundComPortPool inboundComPortPool = comPortPoolReference.orNull();
        return inboundComPortPool == null || inboundComPortPool.getComPortType() == inboundComPortPool.getComPortType();
    }

}
