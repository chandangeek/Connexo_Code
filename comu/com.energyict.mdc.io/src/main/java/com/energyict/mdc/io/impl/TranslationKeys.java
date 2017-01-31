/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.io.ModemProperties;
import com.energyict.mdc.io.naming.ModemPropertySpecNames;
import com.energyict.mdc.io.naming.PEMPModemPropertySpecNames;
import com.energyict.mdc.io.naming.SerialPortConfigurationPropertySpecNames;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-07 (16:36)
 */
public enum TranslationKeys implements TranslationKey {
    SERIAL_FLOWCONTROL(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL, "Flowcontrol"),
    SERIAL_FLOWCONTROL_DESCRIPTION(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL + ".description", "Flowcontrol"),
    SERIAL_BAUDRATE(SerialPortConfigurationPropertySpecNames.BAUDRATE, "Baudrate"),
    SERIAL_BAUDRATE_DESCRIPTION(SerialPortConfigurationPropertySpecNames.BAUDRATE + ".description", "Baudrate"),
    SERIAL_NUMBEROFSTOPBITS(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS, "Number of stop bits"),
    SERIAL_NUMBEROFSTOPBITS_DESCRIPTION(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS + ".description", "The number of stop bits"),
    SERIAL_NUMBEROFDATABITS(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS, "Number of data bits"),
    SERIAL_NUMBEROFDATABITS_DESCRIPTION(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS + ".description", "The number of data bits"),
    SERIAL_PARITY(SerialPortConfigurationPropertySpecNames.PARITY, "Parity"),
    SERIAL_PARITY_DESCRIPTION(SerialPortConfigurationPropertySpecNames.PARITY + ".description", "Parity"),
    DELAY_BEFORE_SEND(ModemPropertySpecNames.DELAY_BEFORE_SEND, "Send delay"),
    DELAY_AFTER_CONNECT(ModemPropertySpecNames.DELAY_AFTER_CONNECT, "Delay after connect"),
    COMMAND_TIMEOUT(ModemPropertySpecNames.COMMAND_TIMEOUT, "Command timeout"),
    CONNECT_TIMEOUT(ModemPropertySpecNames.CONNECT_TIMEOUT, "Connect timeout"),
    COMMAND_TRIES(ModemPropertySpecNames.COMMAND_TRIES, "Number of times a command is attempted"),
    GLOBAL_INIT_STRINGS(ModemPropertySpecNames.GLOBAL_INIT_STRINGS, "Global modem init string"),
    INIT_STRINGS(ModemPropertySpecNames.INIT_STRINGS, "Modem init string"),
    DIAL_PREFIX(ModemPropertySpecNames.DIAL_PREFIX, "Dial prefix"),
    ADDRESS_SELECTOR(ModemPropertySpecNames.ADDRESS_SELECTOR, "Address selector"),
    POST_DIAL_COMMANDS(ModemPropertySpecNames.POST_DIAL_COMMANDS, "Postdial command(s)"),
    DTR_TOGGLE_DELAY(ModemPropertySpecNames.DTR_TOGGLE_DELAY, "Disconnect line toggle delay"),
    PHONE_NUMBER_PROPERTY("ModemProperties." + ModemProperties.PHONE_NUMBER_PROPERTY_NAME, "Phone number"),
    PEMP_CONFIGURATION_KEY("PEMP." + PEMPModemPropertySpecNames.CONFIGURATION_KEY, "Configuration key"),
        ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}