package com.energyict.protocolimplv2;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Specifies the possible DeviceProtocolDialect names.
 *
 * @author sva
 * @since 17/10/12 (10:23)
 */
public enum DeviceProtocolDialectName implements TranslationKey {

    CTR_DEVICE_PROTOCOL("CtrDialect", "CTR"),
    EIWEBPLUS("EIWebPlusDialect", "EIWebPlus dialect"),
    TCP_DLMS_PROTOCOL("TcpDlmsDialect", "TCP/DLMS"),
    BEACON_MIRROR_TCP_DLMS_PROTOCOL("MirrorTcpDlmsDialect", "Beacon mirror TCP/DLMS"),
    BEACON_GATEWAY_TCP_DLMS_PROTOCOL("GatewayTcpDlmsDialect", "Beacon gateway TCP/DLMS"),
    SERIAL_DLMS_PROTOCOL("SerialDlmsDialect", "Serial"),
    DSMR23_DEVICE_PROTOCOL("Dsmr23Dialect", "DSMR 23"),
    SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL("SDKStandardDialect", "SDK dialect (default)"),
    SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL("SDKLoadProfileDialect", "SDK dialect for loadProfile testing"),
    SDK_SAMPLE_TIME_DEVICE_PROTOCOL("SDKTimeDialect", "SDK dialect for time testing"),
    SDK_SAMPLE_TOPOLOGY("SDKTopologyDialect", "SDK dialect for topology testing"),
    SDK_SAMPLE_FIRMWARE("SDKFirmwareDialect", "SDK dialect for firmware testing"),
    SDK_SAMPLE_CALENDAR("SDKCalendarDialect", "SDK dialect for calendar testing"),
    SDK_SAMPLE_BREAKER("SDKBreakerDialect", "SDK dialect for breaker testing"),
    ACE4000_DEVICE_PROTOCOL("ACE4000GprsDialect", "ACE 4000"),
    GARNET_TCP("GarnetTcpDialect", "Garnet TCP"),
    GARNET_SERIAL("GarnetSerialDialect", "Garnet Serial"),
    EDP("EDP", "Serial"),
    ABNT_SERIAL("AbntSerialDialect", "Serial"),
    ABNT_OPTICAL("AbntOpticalDialect", "Optical"),
    NO_PARAMETERS_PROTOCOL("NoParamsDialect", "Default with no properties"),
    TCP("TcpDialect", "TCP Dlms"),
    SERIAL("SerialDialect", "Serial Dlms");

    private final String uniqueName;
    private final String defaultFormat;

    DeviceProtocolDialectName(String uniqueName, String defaultFormat) {
        this.uniqueName = uniqueName;
        this.defaultFormat = defaultFormat;
    }

    public String getName() {
        return uniqueName;
    }


    @Override
    public String getKey() {
        return this.getName();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}