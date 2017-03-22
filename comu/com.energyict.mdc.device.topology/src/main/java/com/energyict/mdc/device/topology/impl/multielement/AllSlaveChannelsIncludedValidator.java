/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.multielement;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class AllSlaveChannelsIncludedValidator implements ConstraintValidator<AllSlaveChannelsIncluded, MultiElementDeviceReferenceImpl> {

    @Override
    public void initialize(AllSlaveChannelsIncluded allSlaveChannelsIncluded) {
        // nothing to do
    }

    @Override
    public boolean isValid(MultiElementDeviceReferenceImpl multiElementDeviceReference, ConstraintValidatorContext constraintValidatorContext) {
        Device slave = multiElementDeviceReference.getOrigin();
        long numberOfChannelUsages = multiElementDeviceReference.getChannelUsages().stream().map(DataLoggerChannelUsage::getSlaveChannel).count();
        int numberOfSlaveChannels = slave.getChannels().size();
        int numberOfSlaveRegisters = slave.getRegisters().size();
        return numberOfChannelUsages == numberOfSlaveChannels + numberOfSlaveRegisters;
    }

}