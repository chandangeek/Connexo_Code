package com.energyict.mdc.device.data.rest.impl;

/**
 * Linking a slave's channel with a data logger's channels
 * The availabilityDate is additional info coming from BE representing the date at which the dataLoggerChannel is free to use: 0 = has never been linked,
 * null: the dataLoggerChannel is not available for linking
 *
 * Copyrights EnergyICT
 * Date: 10/05/2016
 * Time: 10:47
 */
public class DataLoggerSlaveChannelInfo {
    public ChannelInfo slaveChannel;
    public ChannelInfo dataLoggerChannel;
    // representing the date at which the dataLoggerChannel is free to use:
    //  0 = has never been linked
    //  null: already linked => not available
    public Long availabilityDate;

}
