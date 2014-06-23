package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

public class TcpOutboundComPortInfo extends OutboundComPortInfo {

    public TcpOutboundComPortInfo() {
        this.comPortType = ComPortType.TCP;
    }

    public TcpOutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
    }
}
