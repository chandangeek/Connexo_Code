package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.metering.Channel;
import com.energyict.mdc.device.topology.TopologyService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the DataLoggerReference includes all slave channels.
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
        return !dataLoggerReference.getDataLoggerChannelUsages().stream().filter((x) -> isAvailable(x.getDataLoggerChannel())).findAny().isPresent();
    }

    private boolean isAvailable(Channel dataloggerChannel){
        return ((TopologyServiceImpl)this.topologyService).isReferenced(dataloggerChannel);
    }
}