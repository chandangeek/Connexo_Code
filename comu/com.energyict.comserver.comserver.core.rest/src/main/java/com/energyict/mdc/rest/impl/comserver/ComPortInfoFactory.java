package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

public class ComPortInfoFactory {
    public static ComPortInfo asInfo(ComPort comPort, EngineModelService engineModelService) {
        if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return asInboundInfo(comPort);
        } else {
            return asOutboundInfo(comPort, engineModelService);
        }
    }

    public static InboundComPortInfo asInboundInfo(ComPort comPort) {
        if (ComPortType.TCP.equals(comPort.getComPortType())) {
            return new TcpInboundComPortInfo((TCPBasedInboundComPort) comPort);
        }
        if (ComPortType.SERIAL.equals(comPort.getComPortType())) {
            return new ModemInboundComPortInfo((ModemBasedInboundComPort) comPort);
        }
        if (ComPortType.UDP.equals(comPort.getComPortType())) {
            return new UdpInboundComPortInfo((UDPBasedInboundComPort) comPort);
        }
        if (ComPortType.SERVLET.equals(comPort.getComPortType())) {
            return new ServletInboundComPortInfo((ServletBasedInboundComPort) comPort);
        }
        throw new IllegalArgumentException("Unsupported InboundComPort type "+comPort.getClass().getSimpleName());
    }

    public static OutboundComPortInfo asOutboundInfo(ComPort comPort, EngineModelService engineModelService) {
        if (ComPortType.TCP.equals(comPort.getComPortType())) {
            return new TcpOutboundComPortInfo((OutboundComPort) comPort, engineModelService);
        }
        if (ComPortType.UDP.equals(comPort.getComPortType())) {
            return new UdpOutboundComPortInfo((OutboundComPort) comPort, engineModelService);
        }
        if (ComPortType.SERIAL.equals(comPort.getComPortType())) {
            return new ModemOutboundComPortInfo((OutboundComPort) comPort, engineModelService);
        }
        throw new IllegalArgumentException("Unsupported OutboundComPort type "+comPort.getComPortType());
    }
}
