package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.stream.Collectors;

/**
 * Validates that the DataLoggerReference includes all slave channels.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:57 PM
 */
public class AllSlaveChannelsIncludedValidator implements ConstraintValidator<AllSlaveChannelsIncluded, DataLoggerReferenceImpl> {


    @Override
    public void initialize(AllSlaveChannelsIncluded allSlaveChannelsIncluded) {
        // nothing to do
    }

    @Override
    public boolean isValid(DataLoggerReferenceImpl dataLoggerReference, ConstraintValidatorContext constraintValidatorContext) {
        Device slave = dataLoggerReference.getOrigin();
        return dataLoggerReference.getDataLoggerChannelUsages().stream().map(DataLoggerChannelUsage::getSlaveChannel).collect(Collectors.toSet()).size() ==
               slave.getChannels().size()+slave.getRegisters().size();
    }
}