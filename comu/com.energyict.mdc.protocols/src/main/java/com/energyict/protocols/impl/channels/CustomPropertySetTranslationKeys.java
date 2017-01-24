package com.energyict.protocols.impl.channels;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides {@link TranslationKey}s for the names of the
 * {@link CustomPropertySet}s provided by this protocol bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:50)
 */
public enum CustomPropertySetTranslationKeys implements TranslationKey {

    // Connection Types
    EIWEB_CUSTOM_PROPERTY_SET_NAME("EIWebCustomPropertySet", "EIWeb"),
    EIWEB_PLUS_CUSTOM_PROPERTY_SET_NAME("EIWebPlusCustomPropertySet", "EIWeb+"),
    SERIAL_IO_SERIAL_CUSTOM_PROPERTY_SET_NAME("SioSerialCustomPropertySet", "Serial IO serial"),
    SERIAL_IO_AT_CUSTOM_PROPERTY_SET_NAME("SioATModemCustomPropertySet", "Serial IO AT modem"),
    SERIAL_IO_OPTICAL_CUSTOM_PROPERTY_SET_NAME("SioOpticalCustomPropertySet", "Serial IO optical"),
    SERIAL_IO_CASE_CUSTOM_PROPERTY_SET_NAME("SioCaseModemCustomPropertySet", "Serial IO Case modem"),
    SERIAL_IO_PAKNET_CUSTOM_PROPERTY_SET_NAME("SioPaknetModemCustomPropertySet", "Serial IO Paknet modem"),
    SERIAL_IO_PEMP_CUSTOM_PROPERTY_SET_NAME("SioPEMPModemCustomPropertySet", "Serial IO PEMP modem"),
    RX_TX_SERIAL_CUSTOM_PROPERTY_SET_NAME("RxTxSerialCustomPropertySet", "RxTx serial"),
    RX_TX_AT_CUSTOM_PROPERTY_SET_NAME("RxTxATCustomPropertySet", "RxTx AT modem"),
    RX_TX_OPTICAL_CUSTOM_PROPERTY_SET_NAME("RxTxOpticalCustomPropertySet", "RxTx optical"),
    LEGACY_OPTICAL_DLMS_CUSTOM_PROPERTY_SET_NAME("LegacyOpticalDlmsCustomPropertySet", "Legacy optical dlms"),
    CTR_INBOUND_DIAL_HOME_ID_CUSTOM_PROPERTY_SET_NAME("CTRInboundDialHomeIdCustomPropertySet", "CTR Inbound dial home id"),
    INBOUND_PROXIMUS_CUSTOM_PROPERTY_SET_NAME("InboundProximusSmsCustomPropertySet", "Inbound proximus"),
    OUTBOUND_PROXIMUS_CUSTOM_PROPERTY_SET_NAME("OutboundProximusSmsCustomPropertySet", "Outbound proximus"),
    OUTBOUND_TCP_CUSTOM_PROPERTY_SET_NAME("OutboundIpCustomPropertySet", "Outbound TCP/IP"),
    OUTBOUND_TCP_POST_DIAL_CUSTOM_PROPERTY_SET_NAME("OutboundIpPostDialCustomPropertySet", "Outbound TCP/IP (post dial)"),
    OUTBOUND_UDP_CUSTOM_PROPERTY_SET_NAME("OutboundUdpCustomPropertySet", "Outbound UDP");

    private final String key;
    private final String defaultFormat;

    CustomPropertySetTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}