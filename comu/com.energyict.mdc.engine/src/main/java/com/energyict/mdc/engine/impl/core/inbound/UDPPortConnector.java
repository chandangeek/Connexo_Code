package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannelImpl;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.protocols.mdc.channels.ip.datagrams.InboundUdpSession;
import com.energyict.protocols.mdc.services.SocketService;

import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.services.HexService;

/**
 * Implementation of an {@link InboundComPortConnector} for an {@link UDPBasedInboundComPort},
 * i.e. a {@link com.energyict.mdc.engine.model.ComPort} of the type {@link ComPortType#UDP}.
 *
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 15:22
 */
public class UDPPortConnector implements InboundComPortConnector {

    private final InboundUdpSession inboundUdpSession;
    private final HexService hexService;

    public UDPPortConnector(UDPBasedInboundComPort comPort, SocketService socketService, HexService hexService) {
        super();
        this.hexService = hexService;
        this.inboundUdpSession = new InboundUdpSession(comPort.getBufferSize(), comPort.getPortNumber(), socketService);
    }

    @Override
    public ComPortRelatedComChannel accept() {
        return new ComPortRelatedComChannelImpl(this.inboundUdpSession.accept(), this.hexService);
    }

}