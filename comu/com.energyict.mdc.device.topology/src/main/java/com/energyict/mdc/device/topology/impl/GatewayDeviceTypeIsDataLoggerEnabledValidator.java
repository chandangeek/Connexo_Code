/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GatewayDeviceTypeIsDataLoggerEnabledValidator implements ConstraintValidator<GatewayDeviceTypeIsDataLoggerEnabled, DataLoggerReferenceImpl> {


    @Override
    public void initialize(GatewayDeviceTypeIsDataLoggerEnabled originDeviceTypeIsDataLogger) {
        // nothing to do
    }

    @Override
    public boolean isValid(DataLoggerReferenceImpl dataLoggerReference, ConstraintValidatorContext constraintValidatorContext) {
        Device gateway = dataLoggerReference.getGateway();
        return (gateway != null && gateway.getDeviceConfiguration().isDataloggerEnabled());
    }
}