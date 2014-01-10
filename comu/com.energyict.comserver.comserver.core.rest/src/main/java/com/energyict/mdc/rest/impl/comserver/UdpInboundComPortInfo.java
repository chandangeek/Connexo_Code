package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.shadow.ports.UDPBasedInboundComPortShadow;

public class UdpInboundComPortInfo extends InboundComPortInfo<UDPBasedInboundComPort> {

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
    protected void writeTo(UDPBasedInboundComPort source) {
        super.writeTo(source);
        source.setComPortPool(this.comPortPool_id);
        source.setPortNumber(this.portNumber);
        source.setBufferSize(this.bufferSize);
    }

}
