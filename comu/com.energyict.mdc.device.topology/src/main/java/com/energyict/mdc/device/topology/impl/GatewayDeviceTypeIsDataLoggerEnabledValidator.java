package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the gateway which is set on a DataLoggerReferenceImpl is "data logger enabled".
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:57 PM
 */
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