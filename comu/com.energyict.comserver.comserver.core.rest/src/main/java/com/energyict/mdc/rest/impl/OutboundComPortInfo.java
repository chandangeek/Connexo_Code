package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.shadow.ports.ComPortShadow;
import com.energyict.mdc.shadow.ports.OutboundComPortShadow;

public class OutboundComPortInfo extends ComPortInfo {

    public OutboundComPortInfo() {
    }

    public OutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
    }

    @Override
    public ComPortShadow asShadow() {
        OutboundComPortShadow shadow = new OutboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
