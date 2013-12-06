package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.ports.ServletBasedInboundComPort;
import com.energyict.mdc.ports.TCPBasedInboundComPort;
import com.energyict.mdc.ports.UDPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ComPortShadow;

public class ComPortInfoFactory {
    public static ComPortInfo<? extends ComPortShadow> asInfo(ComPort comPort) {
        if (InboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return asInboundInfo(comPort);
        } else {
            return asOutboundInfo(comPort);
        }
    }

    public static InboundComPortInfo<? extends ComPortShadow> asInboundInfo(ComPort comPort) {
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
