package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.protocols.mdc.channels.ip.datagrams.InboundUdpSession;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;

/**
 * Implementation of an {@link InboundComPortConnector} for a {@link com.energyict.mdc.engine.model.ComPort}
 * of the type {@link ComPortType#UDP}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 15:22
 */
public class UDPPortConnector implements InboundComPortConnector {

    /**
     * The used inboundUdpSession
     */
    private final InboundUdpSession inboundUdpSession;

    public UDPPortConnector(UDPBasedInboundComPort comPort) {
        this.inboundUdpSession = new InboundUdpSession(comPort.getBufferSize(), comPort.getPortNumber());
    }

    @Override
    public ComPortRelatedComChannel accept() {
        return new ComPortRelatedComChannelImpl(this.inboundUdpSession.accept());
    }

}