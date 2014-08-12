package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.common.HasId;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 14:24
 */
public interface ComStatistics extends HasId {

    /**
     * Gets the number of bytes sent during the {@link ComSession}
     *
     * @return The number of bytes sent
     */
    public long getNrOfBytesSent();

    /**
     * Gets the number of bytes received during the {@link ComSession}
     *
     * @return The number of bytes received
     */
    public long getNrOfBytesReceived();

    /**
     * Gets the number of packets sent during the {@link ComSession}
     *
     * @return The number of packets sent
     */
    public long getNrOfPacketsSent();

    /**
     * Gets the number of packets received during the {@link ComSession}
     *
     * @return The number of packets received
     */
    public long getNrOfPacketsReceived();

}
