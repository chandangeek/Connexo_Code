package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

public class ComPortInfoFactory {
    public static ComPortInfo asInfo(ComPort comPort, EngineConfigurationService engineConfigurationService) {
        if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return asInboundInfo(comPort);
        } else {
            return asOutboundInfo(comPort, engineConfigurationService);
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

    public static OutboundComPortInfo asOutboundInfo(ComPort comPort, EngineConfigurationService engineConfigurationService) {
        if (ComPortType.TCP.equals(comPort.getComPortType())) {
            return new TcpOutboundComPortInfo((OutboundComPort) comPort, engineConfigurationService);
        }
        if (ComPortType.UDP.equals(comPort.getComPortType())) {
            return new UdpOutboundComPortInfo((OutboundComPort) comPort, engineConfigurationService);
        }
        if (ComPortType.SERIAL.equals(comPort.getComPortType())) {
            return new ModemOutboundComPortInfo((OutboundComPort) comPort, engineConfigurationService);
        }
        throw new IllegalArgumentException("Unsupported OutboundComPort type "+comPort.getComPortType());
    }
}
