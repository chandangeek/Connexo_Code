package com.energyict.protocols.mdc;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.protocols.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.protocols.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.protocols.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.protocols.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.protocols.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.mdc.channels.ip.socket.TcpIpPostDialConnectionType;
import com.energyict.protocols.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.serialio.SioCaseModemConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.serialio.SioPEMPModemConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType;
import com.energyict.protocols.mdc.channels.serial.optical.dlms.LegacyOpticalDlmsConnectionType;
import com.energyict.protocols.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.protocols.mdc.channels.sms.OutboundProximusSmsConnectionType;

/**
 * List all existing ConnectionType pluggable classes
 */
public enum ConnectionTypeRule implements PluggableClassDefinition<ConnectionType> {
    OutboundProximusSms(OutboundProximusSmsConnectionType.class),
    OutboundUdp(OutboundUdpConnectionType.class),
    OutboundTcpIp(OutboundTcpIpConnectionType.class),
    TcpIpPostDial(TcpIpPostDialConnectionType.class),
    RxTxSerial(RxTxSerialConnectionType.class),
    RxTxAtModem(RxTxAtModemConnectionType.class),
    RxTxOptical(RxTxOpticalConnectionType.class),
    SioSerial(SioSerialConnectionType.class),
    SioCaseModem(SioCaseModemConnectionType.class),
    SioAtModem(SioAtModemConnectionType.class),
    SioPEMPModem(SioPEMPModemConnectionType.class),
    SioOptical(SioOpticalConnectionType.class),
    SioPaknetModem(SioPaknetModemConnectionType.class),
    CTRInboundDialHomeId(CTRInboundDialHomeIdConnectionType.class),
    LegacyOpticalDlms(LegacyOpticalDlmsConnectionType.class),
    InboundProximusSms(InboundProximusSmsConnectionType.class),
    InboundIp(InboundIpConnectionType.class),
    EIWeb(EIWebConnectionType.class);

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
