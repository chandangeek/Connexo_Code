/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.energyict.mdc.device.data.tasks.history.ComStatistics;

/**
 * Provides an implementation for the {@link ComStatistics} interface.
 *
 * User: sva
 * Date: 23/04/12
 * Time: 16:02
 */
public class ComStatisticsImpl implements ComStatistics {

    private final long numberOfBytesSent;
    private final long numberOfBytesReceived;
    private final long numberOfPacketsSent;
    private final long numberOfPacketsReceived;

    public ComStatisticsImpl(long numberOfBytesSent, long numberOfBytesReceived, long numberOfPacketsSent, long numberOfPacketsReceived) {
        super();
        this.numberOfBytesSent = numberOfBytesSent;
        this.numberOfBytesReceived = numberOfBytesReceived;
        this.numberOfPacketsSent = numberOfPacketsSent;
        this.numberOfPacketsReceived = numberOfPacketsReceived;
    }

    @Override
    public long getNumberOfBytesSent() {
        return numberOfBytesSent;
    }

    @Override
    public long getNumberOfBytesReceived() {
        return numberOfBytesReceived;
    }

    @Override
    public long getNumberOfPacketsSent() {
        return numberOfPacketsSent;
    }

    @Override
    public long getNumberOfPacketsReceived() {
        return numberOfPacketsReceived;
    }

}