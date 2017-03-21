/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.multielement;

import com.elster.jupiter.metering.Channel;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.impl.PhysicalGatewayReference;

import java.util.List;

public interface MultiElementDeviceReference extends PhysicalGatewayReference {

    boolean addChannelUsage(Channel slaveChannel, Channel dataLoggerChannel);

    List<DataLoggerChannelUsage> getChannelUsages();

    boolean isTerminated();
}
