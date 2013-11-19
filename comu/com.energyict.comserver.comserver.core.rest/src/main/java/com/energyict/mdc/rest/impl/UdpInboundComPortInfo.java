package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.UDPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.ports.UDPBasedInboundComPortShadow;

public class UdpInboundComPortInfo extends ComPortInfo {

    public int comPortPoolId;
    public int portNumber;

    public UdpInboundComPortInfo() {
    }

    public UdpInboundComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
        this.comPortPoolId = comPort.getComPortPool().getId();
        this.portNumber = comPort.getPortNumber();
    }

    @Override
    public ComPortShadow asShadow() {
        UDPBasedInboundComPortShadow shadow = new UDPBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        shadow.setInboundComPortPoolId(this.comPortPoolId);
        shadow.setPortNumber(this.portNumber);
        return shadow;
    }
}
