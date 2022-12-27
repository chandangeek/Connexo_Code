/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Stijn Vanhoorelbeke
 * @since 15.06.17 - 10:54
 */
public enum ConnectionTypePluggableClassTranslationKeys implements TranslationKey {

    EmptyConnectionType("EmptyConnectionType", "None"),
    EIWebPlusConnectionType("EIWebPlusConnectionType", "EIWeb+"),
    EIWebConnectionType("EIWebConnectionType", "EIWeb"),
    CoapConnectionType("CoapConnectionType", "Coap"),
    OutboundProximusSmsConnectionType("OutboundProximusSmsConnectionType", "Outbound SMS [Proximus]"),
    OutboundUdpConnectionType("OutboundUdpConnectionType", "Outbound UDP"),
    WavenisGatewayConnectionType("WavenisGatewayConnectionType", "Wavenis gateway"),
    TLSConnectionType("TLSConnectionType", "Outbound TLS"),
    TLSHsmConnectionType("TLSHSMConnectionType", "Outbound TLS HSM"),
    TcpIpPostDialConnectionType("TcpIpPostDialConnectionType", "Outbound TCP/IP post dial"),
    OutboundTcpIpConnectionType("OutboundTcpIpConnectionType", "Outbound TCP/IP"),
    OutboundTcpIpWithWakeUpConnectionType("OutboundTcpIpWithWakeUpConnectionType", "Outbound TCP/IP with wakeup"),
    InboundIpConnectionType("InboundIpConnectionType", "Inbound IP"),
    CTRInboundDialHomeIdConnectionType("CTRInboundDialHomeIdConnectionType", "Inbound CTR"),
    WavenisSerialConnectionType("WavenisSerialConnectionType", "Wavenis serial"),
    SioPEMPModemConnectionType("SioPEMPModemConnectionType", "Serial PEMP modem"),
    SioPaknetModemConnectionType("SioPaknetModemConnectionType", "Serial PAKNET modem"),
    SioOpticalConnectionType("SioOpticalConnectionType", "Serial optical"),
    SioCaseModemConnectionType("SioCaseModemConnectionType", "Serial CASE modem"),
    SioAtModemConnectionType("SioAtModemConnectionType", "Serial modem"),
    SioSerialConnectionType("SioSerialConnectionType", "Serial"),
    RxTxOpticalConnectionType("RxTxOpticalConnectionType", "RxTx serial optical"),
    RxTxAtModemConnectionType("RxTxAtModemConnectionType", "RxTx serial modem"),
    RxTxSerialConnectionType("RxTxSerialConnectionType", "RxTx serial"),
    InboundProximusSmsConnectionType("InboundProximusSmsConnectionType", "Inbound SMS [Proximus]");

    private String pluggableClassName;
    private String defaultFormat;

    ConnectionTypePluggableClassTranslationKeys(String pluggableClassName, String defaultFormat) {
        this.pluggableClassName = pluggableClassName;
        this.defaultFormat = defaultFormat;
    }

    public String getPluggableClassName() {
        return pluggableClassName;
    }

    @Override
    public String getKey() {
        return pluggableClassName;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static Optional<ConnectionTypePluggableClassTranslationKeys> from(ConnectionTypePluggableClass connectionTypePluggableClass) {
        String translationKey = connectionTypePluggableClass.getTranslationKey();
        return Stream
                .of(values())
                .filter(each -> each.pluggableClassName.equals(translationKey))
                .findAny();
    }

    public static String translationFor(ConnectionTypePluggableClass connectionTypePluggableClass, Thesaurus thesaurus) {
        Optional<ConnectionTypePluggableClassTranslationKeys> translationKey = from(connectionTypePluggableClass);
        return translationKey.isPresent() ? thesaurus.getFormat(translationKey.get()).format() : connectionTypePluggableClass.getTranslationKey();
    }
}
