package com.energyict.protocols.impl.channels;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.upl.io.ConnectionType;

/**
 * List all existing ConnectionType pluggable classes
 */
public enum ConnectionTypeRule implements PluggableClassDefinition<ConnectionType> {

    EmptyConnectionType(com.energyict.mdc.channels.EmptyConnectionType.class),
    EIWebPlusConnectionType(com.energyict.mdc.channels.inbound.EIWebPlusConnectionType.class),
    EIWebConnectionType(com.energyict.mdc.channels.inbound.EIWebConnectionType.class),
    OutboundProximusSmsConnectionType(com.energyict.mdc.channels.sms.OutboundProximusSmsConnectionType.class),
    OutboundUdpConnectionType(com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType.class),
    WavenisGatewayConnectionType(com.energyict.mdc.channels.ip.socket.WavenisGatewayConnectionType.class),
    TLSConnectionType(com.energyict.mdc.channels.ip.socket.TLSConnectionType.class),
    TcpIpPostDialConnectionType(com.energyict.mdc.channels.ip.socket.TcpIpPostDialConnectionType.class),
    OutboundTcpIpConnectionType(com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType.class),
    InboundIpConnectionType(com.energyict.mdc.channels.ip.InboundIpConnectionType.class),
    CTRInboundDialHomeIdConnectionType(com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType.class),
    WavenisSerialConnectionType(com.energyict.mdc.channels.serial.rf.WavenisSerialConnectionType.class),
    SioPEMPModemConnectionType(com.energyict.mdc.channels.serial.modem.serialio.SioPEMPModemConnectionType.class),
    SioPaknetModemConnectionType(com.energyict.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType.class),
    SioOpticalConnectionType(com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType.class),
    SioCaseModemConnectionType(com.energyict.mdc.channels.serial.modem.serialio.SioCaseModemConnectionType.class),
    SioAtModemConnectionType(com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType.class),
    SioSerialConnectionType(com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType.class),
    RxTxOpticalConnectionType(com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType.class),
    RxTxAtModemConnectionType(com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType.class),
    RxTxSerialConnectionType(com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType.class),
    InboundProximusSmsConnectionType(com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType.class);

    private final Class<? extends com.energyict.mdc.upl.io.ConnectionType> connectionTypeClass;

    ConnectionTypeRule(Class<? extends com.energyict.mdc.upl.io.ConnectionType> connectionTypeClass) {
        this.connectionTypeClass = connectionTypeClass;
    }

    @Override
    public String getName() {
        return this.name();
    }

    public Class<? extends com.energyict.mdc.upl.io.ConnectionType> getProtocolTypeClass() {
        return connectionTypeClass;
    }
}