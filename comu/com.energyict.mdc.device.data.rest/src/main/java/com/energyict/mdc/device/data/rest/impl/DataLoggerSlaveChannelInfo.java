/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

public class DataLoggerSlaveChannelInfo {
    public ChannelInfo slaveChannel;
    public ChannelInfo dataLoggerChannel;
    // representing the date at which the dataLoggerChannel is free to use:
    //  0 = has never been linked
    //  null: already linked => not available
    public Long availabilityDate;

}
