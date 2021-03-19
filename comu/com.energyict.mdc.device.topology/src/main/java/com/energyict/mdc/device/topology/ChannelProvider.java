package com.energyict.mdc.device.topology;

import com.elster.jupiter.metering.Channel;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Interface for (data logger/ multi-element) Devices for which their channels can be 'coupled' to another device's channel
 * Copyrights EnergyICT
 * Date: 5/04/2017
 * Time: 9:46
 */
@ProviderType
public interface ChannelProvider  extends PhysicalGatewayReference{

    boolean addDataLoggerChannelUsage(Channel slaveChannel, Channel dataLoggerChannel);

    List<DataLoggerChannelUsage> getDataLoggerChannelUsages();
}
