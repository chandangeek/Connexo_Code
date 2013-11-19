package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.TCPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.TCPBasedInboundComPortShadow;

public class TcpInboundComPortInfo extends ComPortInfo {
    public int comPortPool_id;
    public int portNumber;

    public TcpInboundComPortInfo() {
    }

    public TcpInboundComPortInfo(TCPBasedInboundComPort comPort) {
        super(comPort);
        this.portNumber = comPort.getPortNumber();
        this.comPortPool_id = comPort.getComPortPool().getId();
    }

    public TCPBasedInboundComPortShadow asShadow() {
        TCPBasedInboundComPortShadow shadow = new TCPBasedInboundComPortShadow();
        super.writeToShadow(shadow);
        shadow.setInboundComPortPoolId(this.comPortPool_id);
        shadow.setPortNumber(this.portNumber);
        return shadow;
    }

}
