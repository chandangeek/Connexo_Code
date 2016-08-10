package com.energyict.mdc.device.data.rest.impl;

/**
 * Linking a slave's register with a data logger's register
 * The availabilityDate is additional info coming from BE representing the date at which the dataLoggerChannel is free to use: 0 = has never been linked
 * null: the dataLoggerChannel is not available for linking
 * Copyrights EnergyICT
 * Date: 10/05/2016
 * Time: 10:47
 */
public class DataLoggerSlaveRegisterInfo {

    public RegisterInfo slaveRegister;
    public RegisterInfo dataLoggerRegister;
    // representing the date at which the dataLoggerChannel is free to use: 0 = has never been linked
    public Long availabilityDate;

}
