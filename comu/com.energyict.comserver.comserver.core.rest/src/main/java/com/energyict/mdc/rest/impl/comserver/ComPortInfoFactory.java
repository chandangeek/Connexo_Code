package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;

public class ComPortInfoFactory {
    public static ComPortInfo<? extends ComPort> asInfo(ComPort comPort) {
        if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return asInboundInfo(comPort);
        } else {
            return asOutboundInfo(comPort);
        }
    }

    public static InboundComPortInfo<? extends ComPort> asInboundInfo(ComPort comPort) {
        if (TCPBasedInboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new TcpInboundComPortInfo((TCPBasedInboundComPort) comPort);
        }
        if (ModemBasedInboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new ModemInboundComPortInfo((ModemBasedInboundComPort) comPort);
        }
        if (UDPBasedInboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new UdpInboundComPortInfo((UDPBasedInboundComPort) comPort);
        }
        if (ServletBasedInboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new ServletInboundComPortInfo((ServletBasedInboundComPort) comPort);
        }
        throw new IllegalArgumentException("Unsupported InboundComPort type "+comPort.getClass().getSimpleName());
    }

    public static OutboundComPortInfo asOutboundInfo(ComPort comPort) {
        if (OutboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new OutboundComPortInfo((OutboundComPort) comPort);
        }
        throw new IllegalArgumentException("Unsupported OutboundComPort type "+comPort.getClass().getSimpleName());
    }
}
