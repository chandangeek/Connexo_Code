package com.energyict.protocols.naming;

import com.energyict.mdc.io.naming.SerialPortConfigurationPropertySpecNames;

/**
 * List the name of the properties of all the {@link com.energyict.mdc.protocol.api.ConnectionType}s
 * provided by this bundle so that custom code can refer to them to set and get values of these properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-25 (09:13)
 */
public enum ConnectionTypePropertySpecName {

    CTR_INBOUND_DIAL_HOME_ID("dialHomeId"),

    EIWEB_IP_ADDRESS("ipAddress"),
    EIWEB_MAC_ADDRESS("macAddress"),

    OUTBOUND_IP_HOST("host"),
    OUTBOUND_IP_PORT_NUMBER("portNumber"),
    OUTBOUND_IP_CONNECTION_TIMEOUT("connectionTimeout"),
    OUTBOUND_IP_BUFFER_SIZE("bufferSize"),
    OUTBOUND_IP_POST_DIAL_DELAY_MILLIS("postDialDelayMillis"),
    OUTBOUND_IP_POST_DIAL_COMMAND_ATTEMPTS("postDialCommandAttempts"),
    OUTBOUND_IP_POST_DIAL_COMMAND("postDialCommand"),

    SERIAL_BAUD_RATE(SerialPortConfigurationPropertySpecNames.BAUDRATE),
    SERIAL_NR_OF_DATA_BITS(SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS),
    SERIAL_NR_OF_STOP_BITS(SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS),
    SERIAL_PARITY(SerialPortConfigurationPropertySpecNames.PARITY),
    SERIAL_FLOW_CONTROL(SerialPortConfigurationPropertySpecNames.FLOW_CONTROL),

    INBOUND_PROXIMUS_PHONE_NUMBER("phoneNumber"),
    INBOUND_PROXIMUS_CALL_HOME_ID("callHomeId"),

    OUTBOUND_PROXIMUS_PHONE_NUMBER("phoneNumber"),
    OUTBOUND_PROXIMUS_SOURCE("source"),
    OUTBOUND_PROXIMUS_AUTHENTICATION("authentication"),
    OUTBOUND_PROXIMUS_SERVICE_CODE("serviceCode"),
    OUTBOUND_PROXIMUS_CONNECTION_URL("connectionUrl");

    private final String name;

    ConnectionTypePropertySpecName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}