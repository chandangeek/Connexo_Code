/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.PhysicalGatewayReference;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhysicalGatewayNotSameAsOriginValidator implements ConstraintValidator<PhysicalGatewayNotSameAsOrigin, PhysicalGatewayReference> {

    private String message;

    @Override
    public void initialize(PhysicalGatewayNotSameAsOrigin physicalGatewayNotSameAsOrigin) {
        message = physicalGatewayNotSameAsOrigin.message();
    }

    @Override
    public boolean isValid(PhysicalGatewayReference physicalGatewayReference, ConstraintValidatorContext constraintValidatorContext) {
        Device gateway = physicalGatewayReference.getGateway();
        if (gateway != null && gateway.getId() == physicalGatewayReference.getOrigin().getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("gateway").addConstraintViolation();
            return false;
        }
        return true;
    }

}