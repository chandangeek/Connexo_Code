package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;

public class OutboundComPortInfo extends ComPortInfo<OutboundComPortShadow> {

    public OutboundComPortInfo() {
    }

    public OutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
    }

    @Override
    protected void writeToShadow(OutboundComPortShadow shadow) {
        super.writeToShadow(shadow);
    }

    @Override
    public OutboundComPortShadow asShadow() {
        OutboundComPortShadow shadow = new OutboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
