/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.topology.PhysicalGatewayReference;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class OnlyOneGatewayReferenceAtAnyTimeValidator implements ConstraintValidator<OnlyOneGatewayReferenceAtAnyTime, PhysicalGatewayReference> {
    private final ServerTopologyService topologyService;
    private String message;

    @Inject
    public OnlyOneGatewayReferenceAtAnyTimeValidator(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Override
    public void initialize(OnlyOneGatewayReferenceAtAnyTime annotation) {
        message = annotation.message();
    }

    @Override
    public boolean isValid(PhysicalGatewayReference physicalGatewayReference, ConstraintValidatorContext constraintValidatorContext) {
        Optional<PhysicalGatewayReference> conflict = findConflict(physicalGatewayReference);
        if (conflict.isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("gateway").addConstraintViolation();
            return false;
        }
        return true;
    }

    private Optional<PhysicalGatewayReference> findConflict(PhysicalGatewayReference reference) {
        return topologyService.dataModel().stream(PhysicalGatewayReference.class)
                .filter(Where.where(AbstractPhysicalGatewayReferenceImpl.Field.ORIGIN.fieldName()).isEqualTo(reference.getOrigin()))
                .filter(Where.where(AbstractPhysicalGatewayReferenceImpl.Field.INTERVAL.fieldName()).isEffective(reference.getRange()))
                .filter(Where.where(AbstractPhysicalGatewayReferenceImpl.Field.ID.fieldName()).isNotEqual(reference.getId()))
                .findAny();
    }
}
