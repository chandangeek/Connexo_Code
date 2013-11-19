package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.UDPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.UDPBasedInboundComPortShadow;

public class UdpInboundComPortInfo extends ComPortInfo<UDPBasedInboundComPortShadow> {

    public int comPortPoolId;
    public int portNumber;
    public int bufferSize;

    public UdpInboundComPortInfo() {
    }

    public UdpInboundComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
        this.comPortPoolId = comPort.getComPortPool().getId();
        this.portNumber = comPort.getPortNumber();
        this.bufferSize = comPort.getBufferSize();
    }

    @Override
    protected void writeToShadow(UDPBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setInboundComPortPoolId(this.comPortPoolId);
        shadow.setPortNumber(this.portNumber);
        shadow.setBufferSize(this.bufferSize);
    }

    @Override
    public UDPBasedInboundComPortShadow asShadow() {
        UDPBasedInboundComPortShadow shadow = new UDPBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
