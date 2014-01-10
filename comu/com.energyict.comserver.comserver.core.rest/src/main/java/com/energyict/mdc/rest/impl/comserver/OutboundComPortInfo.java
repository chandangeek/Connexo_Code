package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;

public class OutboundComPortInfo extends ComPortInfo<OutboundComPort> {

    public OutboundComPortInfo() {
        this.direction = "outbound";
    }

    public OutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
        this.direction = "outbound";
    }

    @Override
    protected void writeTo(OutboundComPort source) {
        super.writeTo(source);
        source.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
    }

}
