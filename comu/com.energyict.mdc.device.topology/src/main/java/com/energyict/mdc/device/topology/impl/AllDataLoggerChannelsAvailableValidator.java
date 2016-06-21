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

/**
 * Validates that the DataChannelUsage's data logger channel only is referred once.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:57 PM
 */
public class AllDataLoggerChannelsAvailableValidator implements ConstraintValidator<AllDataLoggerChannelsAvailable, DataLoggerReferenceImpl> {

    @Inject
    private TopologyService topologyService;

    @Override
    public void initialize(AllDataLoggerChannelsAvailable allSlaveChannelsIncluded) {
    }

    @Override
    public boolean isValid(DataLoggerReferenceImpl dataLoggerReference, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        List<DataLoggerChannelUsage> currentUsages =  dataLoggerReference.getDataLoggerChannelUsages();
        Set<Channel> dataLoggerChannels = currentUsages.stream().map(DataLoggerChannelUsage::getDataLoggerChannel).collect(Collectors.toSet());
        return currentUsages.isEmpty() ||
               (dataLoggerChannels.size() == currentUsages.size() && dataLoggerChannels.stream().filter(this::isAvailable).findAny().isPresent());
    }

    public boolean isAvailable(Channel dataloggerChannel){
        return !((TopologyServiceImpl)this.topologyService).isReferenced(dataloggerChannel);
    }
}