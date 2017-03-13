/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.multielement;

import com.energyict.mdc.device.data.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OriginDeviceTypeIsMultiElementSubmeterValidator implements ConstraintValidator<OriginDeviceTypeIsMultiElementSubmeter, SubMeterReferenceImpl> {

    @Override
    public void initialize(OriginDeviceTypeIsMultiElementSubmeter originDeviceTypeIsMultiElementSubmeter) {
       // nothing to do
    }

    @Override
    public boolean isValid(SubMeterReferenceImpl subMeterReference, ConstraintValidatorContext constraintValidatorContext) {
        Device origin = subMeterReference.getOrigin();
        return (origin != null && origin.getDeviceType().isSubmeterElement());
    }

}