package com.energyict.mdc.device.topology;

import com.elster.jupiter.metering.Channel;
import com.energyict.mdc.device.topology.impl.PhysicalGatewayReference;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/06/2016
 * Time: 9:07
 */
public interface DataLoggerReference extends PhysicalGatewayReference {

    boolean addDataLoggerChannelUsage(Channel slaveChannel, Channel dataLoggerChannel);

    List<DataLoggerChannelUsage> getDataLoggerChannelUsages();

    boolean isTerminated();
}
