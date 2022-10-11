package com.energyict.protocolimplv2.coap.crest;

import com.energyict.obis.ObisCode;

public interface CrestSensorConst {
    static final ObisCode OBIS_CODE_SERIAL_NUMBER = ObisCode.fromString("0.0.96.1.0.255");
    static final ObisCode OBIS_CODE_TELECOM_PROVIDER_NAME = ObisCode.fromString("0.0.96.0.2.255");
    static final ObisCode OBIS_CODE_BATTERY_VOLTAGE = ObisCode.fromString("0.0.96.0.3.255");
    static final ObisCode OBIS_CODE_CONNECTION_METHOD = ObisCode.fromString("0.0.96.0.4.255");
    static final ObisCode OBIS_CODE_CELL_ID = ObisCode.fromString("0.0.96.0.5.255");
    static final ObisCode OBIS_CODE_SIGNAL_QUALITY = ObisCode.fromString("0.0.96.0.6.255");
    static final ObisCode OBIS_CODE_NR_OF_TRIES = ObisCode.fromString("0.0.96.0.7.255");
    static final ObisCode OBIS_CODE_MEASUREMENT_SEND_INTERVAL = ObisCode.fromString("0.0.96.0.8.255");
    static final ObisCode OBIS_CODE_FOTA_MESSAGE_COUNTER = ObisCode.fromString("0.0.96.0.9.255");
    static final ObisCode OBIS_CODE_MEMORY_COUNTER = ObisCode.fromString("0.0.96.0.10.255");
    static final ObisCode OBIS_CODE_AIR_TEMPERATURE = ObisCode.fromString("0.0.96.0.11.255");
    static final ObisCode OBIS_CODE_AIR_HUMIDITY = ObisCode.fromString("0.0.96.0.12.255");

    public static final String DEFAULT_LOAD_PROFILE_CHANNEL_OBIS_CODE = "8.0.1.0.0.255";
}
