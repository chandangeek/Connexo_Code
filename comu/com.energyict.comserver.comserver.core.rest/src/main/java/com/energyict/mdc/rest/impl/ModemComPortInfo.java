package com.energyict.mdc.rest.impl;

import com.energyict.mdc.ports.ModemBasedInboundComPort;
import com.energyict.mdc.shadow.ports.ModemBasedInboundComPortShadow;

public class ModemComPortInfo extends ComPortInfo {

    public ModemComPortInfo() {
    }

    public ModemComPortInfo(ModemBasedInboundComPort comPort) {
        super(comPort);
    }

    @Override
    public ModemBasedInboundComPortShadow asShadow() {
        ModemBasedInboundComPortShadow shadow = new ModemBasedInboundComPortShadow();
        this.writeToShadow(shadow);
        return shadow;
    }
}
