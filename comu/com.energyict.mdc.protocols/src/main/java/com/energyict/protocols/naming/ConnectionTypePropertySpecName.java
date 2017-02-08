/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.naming;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.io.naming.SerialPortConfigurationPropertySpecNames;

/**
 * List the name of the properties of all the {@link com.energyict.mdc.protocol.api.ConnectionType}s
 * provided by this bundle so that custom code can refer to them to set and get values of these properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-25 (09:13)
 */
public enum ConnectionTypePropertySpecName implements TranslationKey {

    CTR_INBOUND_DIAL_HOME_ID("dialHomeId", "CTR.inbound.property.dialhomeid", "Dial home id"),

    EIWEB_IP_ADDRESS("ipAddress", "EIWEB.property.ipAddress", "IP address"),
    EIWEB_MAC_ADDRESS("macAddress", "EIWEB.property.macAddress", "MAC address"),

    OUTBOUND_IP_HOST("host", "IP.outbound.property.host", "Host"),
    OUTBOUND_IP_PORT_NUMBER("portNumber", "IP.outbound.property.port", "Port number"),
    OUTBOUND_IP_CONNECTION_TIMEOUT("connectionTimeout", "IP.outbound.property.timeout", "Timeout"),
    OUTBOUND_IP_BUFFER_SIZE("bufferSize", "IP.outbound.property.bufferSize", "Buffer size"),
    OUTBOUND_IP_POST_DIAL_DELAY_MILLIS("postDialDelayMillis", "IP.outbound.postdial.property.delay", "Post dial delay (milli seconds)"),
    OUTBOUND_IP_POST_DIAL_COMMAND_ATTEMPTS("postDialCommandAttempts", "IP.outbound.postdial.property.commandAttempts", "Number of attempts"),
    OUTBOUND_IP_POST_DIAL_COMMAND("postDialCommand", "IP.outbound.postdial.property.command", "Commands"),
    TLS_CLIENT_CERTIFICATE("tlsClientCertificate", "TLS.client.certificate", "TLS client certificate"),

    SERIAL_BAUD_RATE(SerialPortConfigurationPropertySpecNames.BAUDRATE, "SERIAL.property.baudRate", "Baud rate"),
    SERIAL_NR_OF_DATA_BITS(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS, "SERIAL.property.dataBits", "Data bits"),
    SERIAL_NR_OF_STOP_BITS(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS, "SERIAL.property.stopBits", "Stop bits"),
    SERIAL_PARITY(SerialPortConfigurationPropertySpecNames.PARITY, "SERIAL.property.parity", "Parity"),
    SERIAL_FLOW_CONTROL(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL, "SERIAL.property.flowControl", "FlowControl"),

    INBOUND_PROXIMUS_PHONE_NUMBER("phoneNumber", "PROXIMUS.inbound.property.phoneNumber", "Phone number"),
    INBOUND_PROXIMUS_CALL_HOME_ID("callHomeId", "PROXIMUS.inbound.property.callHomeId", "Call home id"),

    OUTBOUND_PROXIMUS_PHONE_NUMBER("phoneNumber", "PROXIMUS.outbound.property.phoneNumber", "Phone number"),
    OUTBOUND_PROXIMUS_SOURCE("source", "PROXIMUS.outbound.property.source", "Source"),
    OUTBOUND_PROXIMUS_AUTHENTICATION("authentication", "PROXIMUS.outbound.property.authentication", "Authentication"),
    OUTBOUND_PROXIMUS_SERVICE_CODE("serviceCode", "PROXIMUS.outbound.property.serviceCode", "Service code"),
    OUTBOUND_PROXIMUS_CONNECTION_URL("connectionUrl", "PROXIMUS.outbound.property.connectionURL", "Connection URL");

    private final String propertySpecName;
    private final String key;
    private final String defaultFormat;

    ConnectionTypePropertySpecName(String propertySpecName, String key, String defaultFormat) {
        this.propertySpecName = propertySpecName;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public String propertySpecName() {
        return propertySpecName;
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