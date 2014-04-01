package com.energyict.mdc.device.data.journal;

import com.energyict.mdc.common.IdBusinessObject;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 14:24
 */
public interface ComStatistics extends IdBusinessObject {

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
    public long getNrOfBytesRead();

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
    public long getNrOfPacketsRead();

}
