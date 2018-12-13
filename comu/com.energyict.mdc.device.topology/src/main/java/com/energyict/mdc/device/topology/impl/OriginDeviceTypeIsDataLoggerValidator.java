/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OriginDeviceTypeIsDataLoggerValidator implements ConstraintValidator<OriginDeviceTypeIsDataLogger, DataLoggerReferenceImpl> {

    @Override
    public void initialize(OriginDeviceTypeIsDataLogger originDeviceTypeIsDataLogger) {
       // nothing to do
    }

    @Override
    public boolean isValid(DataLoggerReferenceImpl dataLoggerReference, ConstraintValidatorContext constraintValidatorContext) {
        Device origin = dataLoggerReference.getOrigin();
        return (origin != null && origin.getDeviceType().isDataloggerSlave());
    }

}