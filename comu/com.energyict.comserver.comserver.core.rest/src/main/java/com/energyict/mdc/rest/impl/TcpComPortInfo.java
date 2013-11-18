package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.TCPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.TCPBasedInboundComPortShadow;

public class TcpComPortInfo extends ComPortInfo {
    public int comPortPool_id;
    public int numberOfSimultaneousConnections;
    public int portNumber;

    public TcpComPortInfo() {
    }

    public TcpComPortInfo(TCPBasedInboundComPort comPort) {
        super(comPort);
        this.numberOfSimultaneousConnections = comPort.getNumberOfSimultaneousConnections();
        this.portNumber = comPort.getPortNumber();
        this.comPortPool_id = comPort.getComPortPool().getId();
    }

    public TCPBasedInboundComPortShadow asShadow() {
        TCPBasedInboundComPortShadow shadow = new TCPBasedInboundComPortShadow();
        super.writeToShadow(shadow);
        shadow.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
        shadow.setInboundComPortPoolId(this.comPortPool_id);
        shadow.setPortNumber(this.portNumber);
        return shadow;
    }

}
