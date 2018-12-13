/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

public class DataLoggerSlaveRegisterInfo {

    public RegisterInfo slaveRegister;
    public RegisterInfo dataLoggerRegister;
    // representing the date at which the dataLoggerChannel is free to use: 0 = has never been linked
    public Long availabilityDate;

}
