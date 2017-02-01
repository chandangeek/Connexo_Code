/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class AllSlaveChannelsIncludedValidator implements ConstraintValidator<AllSlaveChannelsIncluded, DataLoggerReferenceImpl> {

    @Override
    public void initialize(AllSlaveChannelsIncluded allSlaveChannelsIncluded) {
        // nothing to do
    }

    @Override
    public boolean isValid(DataLoggerReferenceImpl dataLoggerReference, ConstraintValidatorContext constraintValidatorContext) {
        Device slave = dataLoggerReference.getOrigin();
        long numberOfChannelUsages = dataLoggerReference.getDataLoggerChannelUsages().stream().map(DataLoggerChannelUsage::getSlaveChannel).count();
        int numberOfSlaveChannels = slave.getChannels().size();
        int numberOfSlaveRegisters = slave.getRegisters().size();
        return numberOfChannelUsages == numberOfSlaveChannels + numberOfSlaveRegisters;
    }

}