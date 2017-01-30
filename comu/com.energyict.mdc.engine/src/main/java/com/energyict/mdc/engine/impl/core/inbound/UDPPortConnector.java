package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.io.InboundCommunicationException;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.upl.io.InboundUdpSession;
import com.energyict.mdc.upl.io.SocketService;

import java.time.Clock;

/**
 * Implementation of an {@link InboundComPortConnector} for an {@link UDPBasedInboundComPort},
 * i.e. a {@link com.energyict.mdc.engine.config.ComPort} of the type {@link ComPortType#UDP}.
 * <p>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 15:22
 */
public class UDPPortConnector implements InboundComPortConnector {

    private final InboundUdpSession inboundUdpSession;
    private final HexService hexService;
    private final Clock clock;
    private final EventPublisher eventPublisher;
    private final InboundComPort comPort;
    private final DeviceMessageService deviceMessageService;

    public UDPPortConnector(UDPBasedInboundComPort comPort, SocketService socketService, HexService hexService, EventPublisher eventPublisher, Clock clock, DeviceMessageService deviceMessageService) {
        super();
        this.comPort = comPort;
        this.hexService = hexService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.inboundUdpSession = socketService.newInboundUdpSession(comPort.getBufferSize(), comPort.getPortNumber());
        this.deviceMessageService = deviceMessageService;
    }

    @Override
    public ComPortRelatedComChannel accept() {
        return new ComPortRelatedComChannelImpl(this.inboundUdpSession.accept(), this.comPort, this.clock, this.deviceMessageService, this.hexService, eventPublisher);
    }

    @Override
    public void close() {
        try {
            this.inboundUdpSession.close();
        } catch (Exception e) {
            throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
        }
    }

}