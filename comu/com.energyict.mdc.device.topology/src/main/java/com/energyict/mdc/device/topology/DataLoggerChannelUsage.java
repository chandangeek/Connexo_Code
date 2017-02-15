/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import com.elster.jupiter.orm.associations.Effectivity;

public interface DataLoggerChannelUsage extends Effectivity {

    /**
     * @return the PhysicalGatewayReference this DataloggerChannelUsage makes part of
     */
    DataLoggerReference getDataLoggerReference();

    /**
     * @return  The data logger's channel that is used
     */
    com.elster.jupiter.metering.Channel getDataLoggerChannel();

    /**
     * @return The slave channel using the data logger's channel
     */
    com.elster.jupiter.metering.Channel getSlaveChannel();

}
