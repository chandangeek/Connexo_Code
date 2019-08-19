/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks.history;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the statistical information that is gathered while communicating
 * with the {@link com.energyict.mdc.common.device.data.Device}.
 *
 * User: sva
 * Date: 23/04/12
 * Time: 14:24
 */
@ProviderType
public interface ComStatistics {

    /**
     * Gets the number of bytes sent while communicating with the {@link com.energyict.mdc.common.device.data.Device}.
     *
     * @return The number of bytes sent
     */
    long getNumberOfBytesSent();

    /**
     * Gets the number of bytes received while communicating with the {@link com.energyict.mdc.common.device.data.Device}.
     *
     * @return The number of bytes received
     */
    long getNumberOfBytesReceived();

    /**
     * Gets the number of packets sent while communicating with the {@link com.energyict.mdc.common.device.data.Device}.
     *
     * @return The number of packets sent
     */
    long getNumberOfPacketsSent();

    /**
     * Gets the number of packets received while communicating with the {@link com.energyict.mdc.common.device.data.Device}.
     *
     * @return The number of packets received
     */
    long getNumberOfPacketsReceived();

}