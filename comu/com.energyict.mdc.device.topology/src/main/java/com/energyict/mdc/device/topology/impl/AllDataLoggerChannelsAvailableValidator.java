/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.metering.Channel;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AllDataLoggerChannelsAvailableValidator implements ConstraintValidator<AllDataLoggerChannelsAvailable, DataLoggerReferenceImpl> {

    @Inject
    private TopologyService topologyService;

    @Override
    public void initialize(AllDataLoggerChannelsAvailable allSlaveChannelsIncluded) {
    }

    @Override
    public boolean isValid(DataLoggerReferenceImpl dataLoggerReference, ConstraintValidatorContext constraintValidatorContext) {

        List<DataLoggerChannelUsage> currentUsages = dataLoggerReference.getDataLoggerChannelUsages();
        Set<Channel> dataLoggerChannels = currentUsages.stream().map(DataLoggerChannelUsage::getDataLoggerChannel).collect(Collectors.toSet());
        boolean isValid = currentUsages.isEmpty() || (dataLoggerChannels.size() == currentUsages.size() && dataLoggerChannels.stream().filter(this::isAvailable).findAny().isPresent());
        if (!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DATA_LOGGER_CHANNEL_ALREADY_REFERENCED + "}").addConstraintViolation();
        }
        return isValid;
    }

    public boolean isAvailable(Channel dataloggerChannel) {
        return !((TopologyServiceImpl) this.topologyService).isReferenced(dataloggerChannel);
    }
}