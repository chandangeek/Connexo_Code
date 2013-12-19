package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.UDPBasedInboundComPortShadow;

public class UdpInboundComPortInfo extends InboundComPortInfo<UDPBasedInboundComPortShadow> {

    public UdpInboundComPortInfo() {
        this.comPortType = ComPortType.UDP;
    }

    public UdpInboundComPortInfo(UDPBasedInboundComPort comPort) {
        super(comPort);
        if (comPort.getComPortPool()!=null) {
            this.comPortPool_id = comPort.getComPortPool().getId();
        }
        this.portNumber = comPort.getPortNumber();
        this.bufferSize = comPort.getBufferSize();
    }

    @Override
    protected void writeToShadow(UDPBasedInboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setInboundComPortPoolId(this.comPortPool_id);
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
