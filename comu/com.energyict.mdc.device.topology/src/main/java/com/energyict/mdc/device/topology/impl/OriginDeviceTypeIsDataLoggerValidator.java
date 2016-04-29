package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the origin device which is set on a DataLoggerReferenceImpl
 * has a 'Data Logger' DeviceType.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:57 PM
 */
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