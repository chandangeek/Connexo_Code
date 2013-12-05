package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.ports.TCPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.TCPBasedInboundComPortShadow;

public class TcpInboundComPortInfo extends InboundComPortInfo<TCPBasedInboundComPortShadow> {

    public TcpInboundComPortInfo() {
        this.comPortType = ComPortType.TCP;
    }

    public TcpInboundComPortInfo(TCPBasedInboundComPort comPort) {
        super(comPort);
        this.portNumber = comPort.getPortNumber();
    }

    protected void writeToShadow(TCPBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setPortNumber(this.portNumber);
    }

    public TCPBasedInboundComPortShadow asShadow() {
        TCPBasedInboundComPortShadow shadow = new TCPBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }


}
