package com.energyict.mdc.device.data.tasks.history;

/**
 * Copyrights EnergyICT
 * Date: 28/04/2014
 * Time: 17:02
 */
public interface BuildsStatistics<S> {

    S addSentBytes(long numberOfBytes);

    S addReceivedBytes(long numberOfBytes);

    S addSentPackets(long numberOfPackets);

    S addReceivedPackets(long numberOfPackets);

}
