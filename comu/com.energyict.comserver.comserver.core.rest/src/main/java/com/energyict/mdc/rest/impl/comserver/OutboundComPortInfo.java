package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;

public class OutboundComPortInfo extends ComPortInfo<OutboundComPort> {

    public OutboundComPortInfo() {
        this.direction = "outbound";
    }

    public OutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
        this.direction = "outbound";
    }

    @Override
    protected void writeTo(OutboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source,engineModelService);
        source.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
    }

    @Override
    protected OutboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return engineModelService.newOutbound(comServer);
    }

}
