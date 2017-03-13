/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.multielement;

import com.energyict.mdc.device.data.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GatewayDeviceTypeIsMultiElementEnabledValidator implements ConstraintValidator<GatewayDeviceTypeIsMultiElementEnabled, SubMeterReferenceImpl> {


    @Override
    public void initialize(GatewayDeviceTypeIsMultiElementEnabled gatewayDeviceTypeIsMultiElementEnabled) {
        // nothing to do
    }

    @Override
    public boolean isValid(SubMeterReferenceImpl subMeterReference, ConstraintValidatorContext constraintValidatorContext) {
        Device gateway = subMeterReference.getGateway();
        return (gateway != null && gateway.getDeviceConfiguration().isMultiElementEnabled());
    }
}