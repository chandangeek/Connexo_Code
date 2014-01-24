package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

public class TcpInboundComPortInfo extends InboundComPortInfo<TCPBasedInboundComPort, TCPBasedInboundComPort.TCPBasedInboundComPortBuilder> {

    public TcpInboundComPortInfo() {
        this.comPortType = ComPortType.TCP;
    }

    public TcpInboundComPortInfo(TCPBasedInboundComPort comPort) {
        super(comPort);
        this.portNumber = comPort.getPortNumber();
    }

    protected void writeTo(TCPBasedInboundComPort source,EngineModelService engineModelService) {
        super.writeTo(source, engineModelService);
        source.setPortNumber(this.portNumber);
    }

    @Override
    protected TCPBasedInboundComPort.TCPBasedInboundComPortBuilder build(TCPBasedInboundComPort.TCPBasedInboundComPortBuilder builder, EngineModelService engineModelService) {
        return super.build(builder.portNumber(portNumber).numberOfSimultaneousConnections(numberOfSimultaneousConnections), engineModelService);
    }

    @Override
    protected TCPBasedInboundComPort createNew(ComServer comServer, EngineModelService engineModelService) {
        return build(comServer.newTCPBasedInboundComPort(), engineModelService).add();
    }
}
