package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;

public class OutboundComPortInfo extends ComPortInfo<OutboundComPortShadow> {

    public OutboundComPortInfo() {
        this.direction = "outbound";
    }

    public OutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
        this.direction = "outbound";
    }

    @Override
    protected void writeToShadow(OutboundComPortShadow shadow) {
        super.writeToShadow(shadow);
        shadow.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
    }

    @Override
    public OutboundComPortShadow asShadow() {
        OutboundComPortShadow shadow = new OutboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
