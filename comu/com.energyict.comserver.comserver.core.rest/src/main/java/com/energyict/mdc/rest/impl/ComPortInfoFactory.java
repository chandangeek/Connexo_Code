package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.ports.TCPBasedInboundComPort;
import com.energyict.mdc.ports.UDPBasedInboundComPort;

public class ComPortInfoFactory {
    public static ComPortInfo asInfo(ComPort comPort) {
        if (TCPBasedInboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new TcpInboundComPortInfo((TCPBasedInboundComPort) comPort);
        }
        if (ModemBasedInboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new ModemInboundComPortInfo((ModemBasedInboundComPort) comPort);
        }
        if (UDPBasedInboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new UdpInboundComPortInfo((UDPBasedInboundComPort) comPort);
        }
        if (OutboundComPort.class.isAssignableFrom(comPort.getClass())) {
            return new OutboundComPortInfo((OutboundComPort) comPort);
        }
        throw new IllegalArgumentException("Unsupported ComPort type "+comPort.getClass().getSimpleName());
    }
}
