package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.engine.impl.core.inbound.SerialPortConnector;
import com.energyict.mdc.engine.impl.core.inbound.TCPPortConnector;
import com.energyict.mdc.engine.impl.core.inbound.UDPPortConnector;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.io.SocketService;

import java.time.Clock;

/**
 * Provides an implementation for the {@link InboundComPortConnectorFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-22 (16:51)
 */
public class InboundComPortConnectorFactoryImpl implements InboundComPortConnectorFactory {

    private final SerialComponentService serialAtComponentService;
    private final SocketService socketService;
    private final HexService hexService;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final DeviceMessageService deviceMessageService;

    public InboundComPortConnectorFactoryImpl(SerialComponentService serialAtComponentService, SocketService socketService, HexService hexService, EventPublisher eventPublisher, Clock clock, DeviceMessageService deviceMessageService) {
        super();
        this.serialAtComponentService = serialAtComponentService;
        this.socketService = socketService;
        this.hexService = hexService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.deviceMessageService = deviceMessageService;
    }

    @Override
    public InboundComPortConnector connectorFor(InboundComPort inboundComPort) {
        if (inboundComPort instanceof ModemBasedInboundComPort) {
            return new SerialPortConnector((ModemBasedInboundComPort) inboundComPort, this.serialAtComponentService, this.hexService, this.eventPublisher, this.clock, this.deviceMessageService);
        }
        else if (inboundComPort instanceof TCPBasedInboundComPort) {
            return new TCPPortConnector((TCPBasedInboundComPort) inboundComPort, this.socketService, this.hexService, this.eventPublisher, this.clock, this.deviceMessageService);
        }
        else if (inboundComPort instanceof UDPBasedInboundComPort) {
            return new UDPPortConnector((UDPBasedInboundComPort) inboundComPort, socketService, this.hexService, this.eventPublisher, this.clock, this.deviceMessageService);
        }
        throw new RuntimeException("Unknown or unsupported inbound comport type: " + inboundComPort.getClass().getName());
    }

}