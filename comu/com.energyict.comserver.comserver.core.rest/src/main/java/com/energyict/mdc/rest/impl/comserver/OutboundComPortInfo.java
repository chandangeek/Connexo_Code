package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPort;

public class OutboundComPortInfo extends ComPortInfo<OutboundComPort, OutboundComPort.OutboundComPortBuilder> {

    public OutboundComPortInfo() {
        this.direction = "outbound";
    }

    public OutboundComPortInfo(OutboundComPort comPort) {
        super(comPort);
        this.direction = "outbound";
    }

    @Override
    protected void writeTo(OutboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source, engineModelService);
        source.setNumberOfSimultaneousConnections(this.numberOfSimultaneousConnections);
    }

    @Override
    protected OutboundComPort.OutboundComPortBuilder build(OutboundComPort.OutboundComPortBuilder builder, EngineModelService engineModelService) {
        return super.build(builder.comPortType(comPortType), engineModelService);
    }

    @Override
    protected OutboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return build(comServer.newOutboundComPort(this.name, this.numberOfSimultaneousConnections), engineModelService).add();
    }

}
