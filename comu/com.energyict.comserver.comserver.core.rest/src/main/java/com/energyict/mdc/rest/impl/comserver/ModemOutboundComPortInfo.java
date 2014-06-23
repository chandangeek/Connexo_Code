package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

public class ModemOutboundComPortInfo extends OutboundComPortInfo {

    public ModemOutboundComPortInfo() {
        this.comPortType = ComPortType.SERIAL;
    }

    public ModemOutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
    }
}
