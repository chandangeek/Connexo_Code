package com.energyict.mdc.io.naming;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;

/**
 * Defines name of {@link com.elster.jupiter.properties.PropertySpec}s
 * that relate to {@link SerialPortConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-04 (12:03)
 */
public class SerialPortConfigurationPropertySpecNames {

    public static final String BAUDRATE = "serialconfig_baudrate";
    public static final String NR_OF_DATA_BITS = "serialconfig_numberofdatabits";
    public static final String NR_OF_STOP_BITS = "serialconfig_numberofstopbits";
    public static final String PARITY = "serialconfig_parity";
    public static final String FLOW_CONTROL = "serialconfig_flowcontrol";

}