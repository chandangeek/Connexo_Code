package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ModemBasedInboundComPortShadow;

public class ModemInboundComPortInfo extends ComPortInfo {

    public ModemInboundComPortInfo() {
    }

    public ModemInboundComPortInfo(ModemBasedInboundComPort comPort) {
        super(comPort);
    }

    @Override
    public ModemBasedInboundComPortShadow asShadow() {
        ModemBasedInboundComPortShadow shadow = new ModemBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
