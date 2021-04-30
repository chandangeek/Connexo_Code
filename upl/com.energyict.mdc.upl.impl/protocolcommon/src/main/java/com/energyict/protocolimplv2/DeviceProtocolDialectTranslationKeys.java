package com.energyict.protocolimplv2;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Specifies the possible DeviceProtocolDialect names.
 * <p/>
 * <p>
 * Each name should be unique, as it is used in RelationTypes! <BR>
 * The length of the name is limited to 24 characters!
 * </p>
 *
 * @author: sva
 * @since: 17/10/12 (10:23)
 */
public enum DeviceProtocolDialectTranslationKeys implements TranslationKey {

    CTR_DEVICE_PROTOCOL_DIALECT_NAME("CtrDialect", "CTR"),
    EIWEBPLUS_DIALECT_NAME("EIWebPlusDialect", "EIWebPlus dialect"),
    TCP_DLMS_PROTOCOL_DIALECT_NAME("TcpDlmsDialect", "TCP DLMS"),
    BEACON_MIRROR_TCP_DLMS_PROTOCOL_DIALECT_NAME("MirrorTcpDlmsDialect", "Beacon DC mirror TCP/DLMS"),
    BEACON_GATEWAY_TCP_DLMS_PROTOCOL_DIALECT_NAME("GatewayTcpDlmsDialect", "Beacon gateway TCP/DLMS"),
    SERIAL_DLMS_PROTOCOL_DIALECT_NAME("SerialDlmsDialect", "Serial DLMS"),
    SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL_DIALECT_NAME("SDKStandardDialect", "SDK dialect (default)"),
    SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL_DIALECT_NAME("SDKLoadProfileDialect", "SDK dialect for loadProfile testing"),
    SDK_SAMPLE_TIME_DEVICE_PROTOCOL_DIALECT_NAME("SDKTimeDialect", "SDK dialect for time testing"),
    SDK_SAMPLE_TOPOLOGY_DIALECT_NAME("SDKTopologyDialect", "SDK dialect for topology testing"),
    SDK_SAMPLE_FIRMWARE("SDKFirmwareDialect", "SDK dialect for firmware testing"),
    SDK_SAMPLE_CALENDAR("SDKCalendarDialect", "SDK dialect for calendar testing"),
    SDK_SAMPLE_BREAKER("SDKBreakerDialect", "SDK dialect for breaker testing"),
    SDK_SAMPLE_CREDIT("SDKCreditDialect", "SDK dialect for credit testing"),
    SDK_SAMPLE_DEVICE_ALARM_DIALECT_NAME("SDKDeviceAlarmDialect", "SDK dialect for device alarm testing"),
    ACE4000_DEVICE_PROTOCOL_DIALECT_NAME("ACE4000GprsDialect", "ACE 4000 GPRS"),
    GARNET_TCP_DIALECT_NAME("GarnetTcpDialect", "Garnet TCP"),
    GARNET_SERIAL_DIALECT_NAME("GarnetSerialDialect", "Garnet serial"),
    ABNT_SERIAL_DIALECT_NAME("AbntSerialDialect", "Abnt serial"),
    ABNT_OPTICAL_DIALECT_NAME("AbntOpticalDialect", "Abnt optical"),
    ABNT_TRANSPARENT_DIALECT_NAME("AbntTransparentDialect", "Abnt transparent"),
    EDMI_TCP_DIALECT_NAME("EdmiTcpDialect", "EDMI TCP"),
    EDMI_UDP_DIALECT_NAME("EdmiUdpDialect", "EDMI UDP"),
    EDMI_MODEM_DIALECT_NAME("EdmiModemDialect", "EDMI modem"),
    EDMI_SERIAL_DIALECT_NAME("EdmiSerialDialect", "EDMI serial"),
    NO_PARAMETERS_PROTOCOL_DIALECT_NAME("NoParamsDialect", "Default with no properties"),
    TCP_MERCURY_DIALECT_NAME("TcpMercuryDialect", "Mercury TCP");

    private final String defaultTranslation;
    private String uniqueName;

    public final static int MAX_NAME_LENGTH = 24;

    DeviceProtocolDialectTranslationKeys(String uniqueName, String defaultTranslation) {
        this.uniqueName = uniqueName;
        this.defaultTranslation = defaultTranslation;
    }

    public String getName() {
        return uniqueName;
    }

    @Override
    public String getKey() {
        return getName();
    }

    @Override
    public String getDefaultFormat() {
        return defaultTranslation;
    }
}