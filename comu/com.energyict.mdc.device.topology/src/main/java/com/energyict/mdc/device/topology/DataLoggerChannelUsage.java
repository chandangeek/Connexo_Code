package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.topology.impl.DataLoggerReferenceImpl;

/**
 * Represents the usage of a Data Logger channel by a slave channel
 * Copyrights EnergyICT
 * Date: 28/04/2016
 * Time: 11:05
 */
public interface DataLoggerChannelUsage {

    /**
     * @return the PhysicalGatewayReference this DataloggerChannelUsage makes part of
     */
    DataLoggerReferenceImpl getDataLoggerReference();

    /**
     * @return  The data logger's channel that is used
     */
    com.elster.jupiter.metering.Channel getDataLoggerChannel();

    /**
     * @return The slave channel using the data logger's channel
     */
    com.elster.jupiter.metering.Channel getSlaveChannel();

}
