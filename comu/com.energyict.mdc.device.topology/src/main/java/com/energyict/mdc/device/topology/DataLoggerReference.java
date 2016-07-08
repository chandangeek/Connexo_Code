package com.energyict.mdc.device.topology;

import com.energyict.mdc.device.topology.impl.PhysicalGatewayReference;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/06/2016
 * Time: 9:07
 */
public interface DataLoggerReference extends PhysicalGatewayReference {

    List<DataLoggerChannelUsage> getDataLoggerChannelUsages();

    boolean isTerminated();
}
