package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

public class UdpOutboundComPortInfo extends OutboundComPortInfo {

    public UdpOutboundComPortInfo() {
        this.comPortType = ComPortType.UDP;
    }

    public UdpOutboundComPortInfo(OutboundComPort comPort, EngineModelService engineModelService) {
        super(comPort, engineModelService);
    }
}
