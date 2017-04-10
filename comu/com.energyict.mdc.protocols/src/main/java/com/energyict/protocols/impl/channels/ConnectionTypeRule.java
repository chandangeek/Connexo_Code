/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.protocols.impl.channels.inbound.CTRInboundDialHomeIdConnectionType;
import com.energyict.protocols.impl.channels.inbound.EIWebConnectionType;
import com.energyict.protocols.impl.channels.ip.InboundIpConnectionType;
import com.energyict.protocols.impl.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTlsConnectionType;
import com.energyict.protocols.impl.channels.ip.socket.TcpIpPostDialConnectionType;
import com.energyict.protocols.impl.channels.serial.direct.rxtx.RxTxPlainSerialConnectionType;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioPlainSerialConnectionType;
import com.energyict.protocols.impl.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.protocols.impl.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.protocols.impl.channels.serial.modem.serialio.SioCaseModemConnectionType;
import com.energyict.protocols.impl.channels.serial.modem.serialio.SioPEMPModemConnectionType;
import com.energyict.protocols.impl.channels.serial.modem.serialio.SioPaknetModemConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.impl.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.protocols.impl.channels.sms.OutboundProximusSmsConnectionType;

/**
 * List all existing ConnectionType pluggable classes
 */
public enum ConnectionTypeRule implements PluggableClassDefinition<ConnectionType> {
    OutboundProximusSms(OutboundProximusSmsConnectionType.class),
    OutboundUdp(OutboundUdpConnectionType.class),
    OutboundTcpIp(OutboundTcpIpConnectionType.class),
    OutboundTls(OutboundTlsConnectionType.class),
    TcpIpPostDial(TcpIpPostDialConnectionType.class),
    RxTxSerial(RxTxPlainSerialConnectionType.class),
    RxTxAtModem(RxTxAtModemConnectionType.class),
    RxTxOptical(RxTxOpticalConnectionType.class),
    SioSerial(SioPlainSerialConnectionType.class),
    SioCaseModem(SioCaseModemConnectionType.class),
    SioAtModem(SioAtModemConnectionType.class),
    SioPEMPModem(SioPEMPModemConnectionType.class),
    SioOptical(SioOpticalConnectionType.class),
    SioPaknetModem(SioPaknetModemConnectionType.class),
    CTRInboundDialHomeId(CTRInboundDialHomeIdConnectionType.class),
    LegacyOpticalDlms(LegacyOpticalDlmsConnectionType.class),
    InboundProximusSms(InboundProximusSmsConnectionType.class),
    InboundIp(InboundIpConnectionType.class),
    EIWeb(EIWebConnectionType.class),
    Empty(EmptyConnectionType.class);

    private final Class<? extends ConnectionType> connectionTypeClass;

    ConnectionTypeRule(Class<? extends ConnectionType> connectionTypeClass) {
        this.connectionTypeClass = connectionTypeClass;
    }

    @Override
    public String getName() {
        return this.name();
    }

    public Class<? extends ConnectionType> getProtocolTypeClass() {
        return connectionTypeClass;
    }

}