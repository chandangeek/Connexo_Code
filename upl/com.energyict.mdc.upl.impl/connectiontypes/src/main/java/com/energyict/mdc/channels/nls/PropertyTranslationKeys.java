package com.energyict.mdc.channels.nls;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Contains all the {@link TranslationKey}s for the properties (and descriptions) of all the connection types.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-27 (10:28)
 */
public enum PropertyTranslationKeys implements TranslationKey {
    DLMS_ADDRESSING_MODE("upl.property.dlms.addressingMode", "Adressing mode"),
    DLMS_ADDRESSING_MODE_DESCRIPTION("upl.property.dlms.addressingMode.description", "Adressing mode"),
    DLMS_SERVER_MAC_ADDRESS("upl.property.dlms.macAddress", "MAC address"),
    DLMS_SERVER_MAC_ADDRESS_DESCRIPTION("upl.property.dlms.macAddress.description", "MAC address"),
    DLMS_SERVER_UPPER_MAC_ADDRESS("upl.property.dlms.upper.macAddress", "Upper MAC address"),
    DLMS_SERVER_UPPER_MAC_ADDRESS_DESCRIPTION("upl.property.dlms.upper.macAddress.description", "Upper MAC address"),
    DLMS_SERVER_LOWER_MAC_ADDRESS("upl.property.dlms.lower.macAddress", "Lower MAC address"),
    DLMS_SERVER_LOWER_MAC_ADDRESS_DESCRIPTION("upl.property.dlms.lower.macAddress.description", "Lower MAC address"),
    DLMS_CONNECTION("upl.property.dlms.connection", "Connection"),
    DLMS_CONNECTION_DESCRIPTION("upl.property.dlms.connection.description", "Connection"),
    EIWEB_PLUS("upl.property.ipAddress", "IP address"),
    EIWEB_PLUS_DESCRIPTION("upl.property.description.ipAddress", "IP address"),
    EIWEB_IP_ADDRESS("upl.property.eiweb.ipAddress", "IP address"),
    EIWEB_IP_ADDRESS_DESCRIPTION("upl.property.eiweb.ipAddress.description", "IP address"),
    EIWEB_MAC_ADDRESS("upl.property.eiweb.macAddress", "MAC address"),
    EIWEB_MAC_ADDRESS_DESCRIPTION("upl.property.eiweb.macAddress.description", "MAC address"),
    CTR_INBOUND_DIAL_HOME_ID("upl.property.ctr.dialHomeId", "Dial home id"),
    CTR_INBOUND_DIAL_HOME_ID_DESCRIPTION("upl.property.ctr.dialHomeId.description", "Dial home id"),
    OUTBOUND_IP_HOST("upl.property.ip.outbound.host", "Host"),
    OUTBOUND_IP_HOST_DESCRIPTION("upl.property.ip.outbound.host.description", "Host"),
    OUTBOUND_IP_PORT_NUMBER("upl.property.ip.outbound.port", "Port number"),
    OUTBOUND_IP_PORT_NUMBER_DESCRIPTION("upl.property.ip.outbound.port.description", "Port number"),
    OUTBOUND_IP_CONNECTION_TIMEOUT("upl.property.ip.outbound.timeout", "Timeout"),
    OUTBOUND_IP_CONNECTION_TIMEOUT_DESCRIPTION("upl.property.ip.outbound.timeout.description", "Timeout"),
    OUTBOUND_IP_BUFFER_SIZE("upl.property.ip.outbound.bufferSize", "Buffer size"),
    OUTBOUND_IP_BUFFER_SIZE_DESCRIPTION("upl.property.ip.outbound.bufferSize.description", "Buffer size"),
    OUTBOUND_IP_POST_DIAL_DELAY_MILLIS("upl.property.ip.outbound.postdial.delay", "Post dial delay (milli seconds)"),
    OUTBOUND_IP_POST_DIAL_DELAY_MILLIS_DESCRIPTION("upl.property.ip.outbound.postdial.delay.description", "Post dial delay (milli seconds)"),
    OUTBOUND_IP_POST_DIAL_COMMAND_ATTEMPTS("upl.property.ip.outbound.postdial.commandAttempts", "Number of attempts"),
    OUTBOUND_IP_POST_DIAL_COMMAND_ATTEMPTS_DESCRIPTION("upl.property.ip.outbound.postdial.commandAttempts.description", "Number of attempts"),
    OUTBOUND_IP_POST_DIAL_COMMAND("upl.property.ip.outbound.postdial.command", "Commands"),
    OUTBOUND_IP_POST_DIAL_COMMAND_DESCRIPTION("upl.property.ip.outbound.postdial.command.description", "Commands"),
    TLS_PREFERRED_CIPHER_SUITES("upl.property.tls.preferred.cipher.suites", "Preferred cipher suites"),
    TLS_PREFERRED_CIPHER_SUITES_DESCRIPTION("upl.property.tls.preferred.cipher.suites.description", "Preferred cipher suites"),
    TLS_CLIENT_TLS_ALIAS("upl.property.tls.client.tls.alias", "Client tls alias"),
    TLS_CLIENT_TLS_ALIAS_DESCRIPTION("upl.property.tls.client.tls.alias.description", "Client tls alias"),
    TLS_VERSION("upl.property.tls.version", "Version"),
    TLS_VERSION_DESCRIPTION("upl.property.tls.version.description", "Version"),
    SERIAL_FLOWCONTROL("upl.property.serial.flowcontrol", "Flowcontrol"),
    SERIAL_FLOWCONTROL_DESCRIPTION("upl.property.serial.flowcontrol.description", "Flowcontrol"),
    SERIAL_BAUDRATE("upl.property.serial.baudrate", "Baudrate"),
    SERIAL_BAUDRATE_DESCRIPTION("upl.property.serial.baudrate.description", "Baudrate"),
    SERIAL_NUMBEROFSTOPBITS("upl.property.serial.numberOfStopBits", "Number of stop bits"),
    SERIAL_NUMBEROFSTOPBITS_DESCRIPTION("upl.property.serial.numberOfStopBits.description", "The number of stop bits"),
    SERIAL_NUMBEROFDATABITS("upl.property.serial.numberOfDataBits", "Number of data bits"),
    SERIAL_NUMBEROFDATABITS_DESCRIPTION("upl.property.serial.numberOfDataBits.description", "The number of data bits"),
    SERIAL_PARITY("upl.property.serial.parity", "Parity"),
    SERIAL_PARITY_DESCRIPTION("upl.property.serial.parity.description", "Parity"),
    SERIAL_MODEM_DELAY_BEFORE_SEND("upl.property.serial.modem.delayBeforeSend", "Send delay"),
    SERIAL_MODEM_DELAY_BEFORE_SEND_DESCRIPTION("upl.property.serial.modem.delayBeforeSend.description", "Send delay"),
    SERIAL_MODEM_DELAY_AFTER_CONNECT("upl.property.serial.modem.delayAfterConnect", "Delay after connect"),
    SERIAL_MODEM_DELAY_AFTER_CONNECT_DESCRIPTION("upl.property.serial.modem.delayAfterConnect.description", "Delay after connect"),
    SERIAL_MODEM_COMMAND_TIMEOUT("upl.property.serial.modem.commandTimeOut", "Command timeout"),
    SERIAL_MODEM_COMMAND_TIMEOUT_DESCRIPTION("upl.property.serial.modem.commandTimeOut.description", "Command timeout"),
    SERIAL_MODEM_CONNECT_TIMEOUT("upl.property.serial.modem.connectTimeOut", "Connect timeout"),
    SERIAL_MODEM_CONNECT_TIMEOUT_DESCRIPTION("upl.property.serial.modem.connectTimeOut.description", "Connect timeout"),
    SERIAL_MODEM_COMMAND_TRIES("upl.property.serial.modem.commandTries", "Number of times a command is attempted"),
    SERIAL_MODEM_COMMAND_TRIES_DESCRIPTION("upl.property.serial.modem.commandTries.description", "Number of times a command is attempted"),
    SERIAL_MODEM_GLOBAL_INIT_STRINGS("upl.property.serial.modem.globalInitStrings", "Global modem init string"),
    SERIAL_MODEM_GLOBAL_INIT_STRINGS_DESCRIPTION("upl.property.serial.modem.globalInitStrings.description", "Global modem init string"),
    SERIAL_MODEM_INIT_STRINGS("upl.property.serial.modem.itStrings", "Modem init string"),
    SERIAL_MODEM_INIT_STRINGS_DESCRIPTION("upl.property.serial.modem.itStrings.description", "Modem init string"),
    SERIAL_MODEM_DIAL_PREFIX("upl.property.serial.modem.dialPrefix", "Dial prefix"),
    SERIAL_MODEM_DIAL_PREFIX_DESCRIPTION("upl.property.serial.modem.dialPrefix.description", "Dial prefix"),
    SERIAL_MODEM_ADDRESS_SELECTOR("upl.property.serial.modem.addressSelector", "Address selector"),
    SERIAL_MODEM_ADDRESS_SELECTOR_DESCRIPTION("upl.property.serial.modem.addressSelector.description", "Address selector"),
    //POST_DIAL_COMMANDS(ModemPropertySpecNames.POST_DIAL_COMMANDS, "Postdial command(s)"),
    SERIAL_MODEM_DTR_TOGGLE_DELAY("upl.property.serial.modem.dtrToggleDelay", "Disconnect line toggle delay"),
    SERIAL_MODEM_DTR_TOGGLE_DELAY_DESCRIPTION("upl.property.serial.modem.dtrToggleDelay.description", "Disconnect line toggle delay"),
    SERIAL_MODEM_PHONE_NUMBER_PROPERTY("upl.property.serial.modem.phoneNumber", "Phone number"),
    SERIAL_MODEM_PHONE_NUMBER_PROPERTY_DESCRIPTION("upl.property.serial.modem.phoneNumber.description", "Phone number"),
    SERIAL_MODEM_PEMP_CONFIGURATION_KEY("upl.property.serial.modem.pemp.configurationKey", "Configuration key"),
    SERIAL_MODEM_PEMP_CONFIGURATION_KEY_DESCRIPTION("upl.property.serial.modem.pemp.configurationKey.description", "Configuration key"),
    SERIAL_RF_ADDRESS("upl.property.serial.rf.address", "RF Address"),
    SERIAL_RF_ADDRESS_DESCRIPTION("upl.property.serial.rf.address.description", "RF Address"),

    INBOUND_PROXIMUS_PHONE_NUMBER("upl.property.proximus.inbound.phoneNumber", "Phone number"),
    INBOUND_PROXIMUS_PHONE_NUMBER_DESCRIPTION("upl.property.proximus.inbound.phoneNumber.description", "Phone number"),
    INBOUND_PROXIMUS_CALL_HOME_ID("upl.property.proximus.inbound.callHomeId", "Call home id"),
    INBOUND_PROXIMUS_CALL_HOME_ID_DESCRIPTION("upl.property.proxiums.inbound.callHomeId.description", "Call home id"),

    OUTBOUND_PROXIMUS_PHONE_NUMBER("upl.property.proximus.outbound.phoneNumber", "Phone number"),
    OUTBOUND_PROXIMUS_PHONE_NUMBER_DESCRIPTION("upl.property.proximus.outbound.phoneNumber.description", "Phone number"),
    OUTBOUND_PROXIMUS_SOURCE("upl.property.proximus.outbound.source", "Source"),
    OUTBOUND_PROXIMUS_SOURCE_DESCRIPTION("upl.property.proximus.outbound.source.description", "Source"),
    OUTBOUND_PROXIMUS_AUTHENTICATION("upl.property.proximus.outbound.authentication", "Authentication"),
    OUTBOUND_PROXIMUS_AUTHENTICATION_DESCRIPTION("upl.property.proximus.outbound.authentication.description", "Authentication"),
    OUTBOUND_PROXIMUS_SERVICE_CODE("upl.property.proximus.outbound.serviceCode", "Service code"),
    OUTBOUND_PROXIMUS_SERVICE_CODE_DESCRIPTION("upl.property.proximus.outbound.serviceCode.description", "Service code"),
    OUTBOUND_PROXIMUS_CONNECTION_URL("upl.property.proximus.outbound.connectionURL", "Connection URL"),
    OUTBOUND_PROXIMUS_CONNECTION_URL_DESCRIPTION("upl.property.proximus.outbound.connectionURL.description", "Connection URL"),

    SERIAL_NR_OF_STOP_BITS("upl.property.serial.nrOfStopBits", "Nr of stop bits"),
    SERIAL_NR_OF_STOP_BITS_DESCRIPTION("upl.property.serial.nrOfStopBits.description", "Nr of stop bits")
    ;

    private final String key;
    private final String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
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