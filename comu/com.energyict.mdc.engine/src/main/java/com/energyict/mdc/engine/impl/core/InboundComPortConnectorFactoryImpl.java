package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.engine.impl.core.inbound.SerialPortConnector;
import com.energyict.mdc.engine.impl.core.inbound.TCPPortConnector;
import com.energyict.mdc.engine.impl.core.inbound.UDPPortConnector;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.services.HexService;

import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.SocketService;

/**
 * Provides an implementation for the {@link InboundComPortConnectorFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-22 (16:51)
 */
public class InboundComPortConnectorFactoryImpl implements InboundComPortConnectorFactory {

    private final SerialComponentService serialComponentService;
    private final SocketService socketService;
    private final HexService hexService;

    public InboundComPortConnectorFactoryImpl(SerialComponentService serialComponentService, SocketService socketService, HexService hexService) {
        super();
        this.serialComponentService = serialComponentService;
        this.socketService = socketService;
        this.hexService = hexService;
    }

    @Override
    public InboundComPortConnector connectorFor(InboundComPort inboundComPort) {
        if (inboundComPort instanceof ModemBasedInboundComPort) {
            return new SerialPortConnector((ModemBasedInboundComPort) inboundComPort, this.serialComponentService, this.hexService);
        }
        else if (inboundComPort instanceof TCPBasedInboundComPort) {
            return new TCPPortConnector((TCPBasedInboundComPort) inboundComPort, this.socketService, this.hexService);
        }
        else if (inboundComPort instanceof UDPBasedInboundComPort) {
            return new UDPPortConnector((UDPBasedInboundComPort) inboundComPort, socketService, this.hexService);
        }
        throw new RuntimeException("Unknown or unsupported inbound comport type: " + inboundComPort.getClass().getName());
    }

}