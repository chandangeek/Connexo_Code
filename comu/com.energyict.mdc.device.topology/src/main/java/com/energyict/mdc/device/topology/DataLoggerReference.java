/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import com.elster.jupiter.metering.Channel;
import com.energyict.mdc.device.topology.impl.PhysicalGatewayReference;

import java.util.List;

public interface DataLoggerReference extends PhysicalGatewayReference {

    boolean addDataLoggerChannelUsage(Channel slaveChannel, Channel dataLoggerChannel);

    List<DataLoggerChannelUsage> getDataLoggerChannelUsages();

    boolean isTerminated();
}
