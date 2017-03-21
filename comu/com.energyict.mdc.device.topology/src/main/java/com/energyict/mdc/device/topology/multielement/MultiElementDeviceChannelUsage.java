/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.multielement;

import com.elster.jupiter.orm.associations.Effectivity;
import com.energyict.mdc.device.topology.DataLoggerReference;

public interface MultiElementDeviceChannelUsage extends Effectivity {

    /**
     * @return the PhysicalGatewayReference this DataloggerChannelUsage makes part of
     */
    MultiElementDeviceReference getMultiElementDeviceReference();

    /**
     * @return  The data logger's channel that is used
     */
    com.elster.jupiter.metering.Channel getDataLoggerChannel();

    /**
     * @return The slave channel using the data logger's channel
     */
    com.elster.jupiter.metering.Channel getSlaveChannel();

}
